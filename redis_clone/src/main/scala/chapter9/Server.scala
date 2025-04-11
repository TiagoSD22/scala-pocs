package chapter9

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.{SelectionKey, Selector, ServerSocketChannel, SocketChannel}
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

class Connection(val channel: SocketChannel) {
  var incoming: mutable.ArrayBuffer[Byte] = mutable.ArrayBuffer.empty
  var outgoing: mutable.ArrayBuffer[Byte] = mutable.ArrayBuffer.empty
  var wantClose: Boolean = false
  var wantRead: Boolean = true
  var wantWrite: Boolean = false
}

class Server(port: Int) {
  private val kMaxMsg = 32 << 20
  private val selector = Selector.open()
  private val serverChannel = ServerSocketChannel.open()
  serverChannel.bind(new InetSocketAddress(port))
  serverChannel.configureBlocking(false)
  serverChannel.register(selector, SelectionKey.OP_ACCEPT)

  private val connections = mutable.Map[Int, Connection]()
  private val dataStore = mutable.Map[String, String]()

  def start(): Unit = {
    while (true) {
      selector.select()
      val selectedKeys = selector.selectedKeys().iterator()
      while (selectedKeys.hasNext) {
        val key = selectedKeys.next()
        selectedKeys.remove()

        if (key.isAcceptable) handleAccept(key)
        if (key.isReadable) handleRead(key)
        if (key.isWritable) handleWrite(key)
      }
    }
  }

  private def handleAccept(key: SelectionKey): Unit = {
    val serverChannel = key.channel().asInstanceOf[ServerSocketChannel]
    val clientChannel = serverChannel.accept()
    clientChannel.configureBlocking(false)
    clientChannel.register(selector, SelectionKey.OP_READ)

    val conn = new Connection(clientChannel)
    connections(clientChannel.hashCode()) = conn
    println(s"New client connected: ${clientChannel.getRemoteAddress}")
  }

  private def handleRead(key: SelectionKey): Unit = {
    val clientChannel = key.channel().asInstanceOf[SocketChannel]
    val conn = connections(clientChannel.hashCode())
    val buffer = ByteBuffer.allocate(64 * 1024)

    val bytesRead = clientChannel.read(buffer)
    if (bytesRead == -1) {
      conn.wantClose = true
      return
    }

    buffer.flip()
    conn.incoming ++= buffer.array().take(buffer.limit())
    while (tryOneRequest(conn)) {}
  }

  private def handleWrite(key: SelectionKey): Unit = {
    val clientChannel = key.channel().asInstanceOf[SocketChannel]
    val conn = connections(clientChannel.hashCode())

    val buffer = ByteBuffer.wrap(conn.outgoing.toArray)
    val bytesWritten = clientChannel.write(buffer)
    conn.outgoing = conn.outgoing.drop(bytesWritten)

    if (conn.outgoing.isEmpty) {
      conn.wantWrite = false
      conn.wantRead = true
      key.interestOps(SelectionKey.OP_READ)
    }
  }

  private def tryOneRequest(conn: Connection): Boolean = {
    if (conn.incoming.length < 4) return false

    val len = ByteBuffer.wrap(conn.incoming.take(4).toArray).getInt
    if (len > kMaxMsg) {
      println("Message too long")
      conn.wantClose = true
      return false
    }

    if (conn.incoming.length < 4 + len) return false

    val request = conn.incoming.slice(4, 4 + len).toArray
    conn.incoming = conn.incoming.drop(4 + len)

    val cmd = parseRequest(request)
    val response = mutable.ArrayBuffer[Byte]()
    doRequest(cmd, response)

    conn.outgoing ++= response
    conn.wantWrite = true
    conn.wantRead = false
    true
  }

  private def parseRequest(data: Array[Byte]): List[String] = {
    val buffer = ByteBuffer.wrap(data)
    val nstr = buffer.getInt
    (0 until nstr).map { _ =>
      val len = buffer.getInt
      val strBytes = new Array[Byte](len)
      buffer.get(strBytes)
      new String(strBytes)
    }.toList
  }

  private def doRequest(cmd: List[String], out: mutable.ArrayBuffer[Byte]): Unit = {
    cmd match {
      case "get" :: key :: Nil =>
        dataStore.get(key) match {
          case Some(value) => out ++= serializeString(value)
          case None        => out ++= serializeNil()
        }
      case "set" :: key :: value :: Nil =>
        dataStore(key) = value
        out ++= serializeNil()
      case "del" :: key :: Nil =>
        val removed = dataStore.remove(key).isDefined
        out ++= serializeInt(if (removed) 1 else 0)
      case "keys" :: Nil =>
        out ++= serializeArray(dataStore.keys.toList)
      case _ =>
        out ++= serializeError("Unknown command")
    }
  }

  private def serializeNil(): Array[Byte] = Array(0.toByte)

  private def serializeString(value: String): Array[Byte] = {
    val bytes = value.getBytes
    val buffer = ByteBuffer.allocate(5 + bytes.length)
    buffer.put(2.toByte)
    buffer.putInt(bytes.length)
    buffer.put(bytes)
    buffer.array()
  }

  private def serializeInt(value: Int): Array[Byte] = {
    val buffer = ByteBuffer.allocate(5)
    buffer.put(3.toByte)
    buffer.putInt(value)
    buffer.array()
  }

  private def serializeArray(values: List[String]): Array[Byte] = {
    val buffer = ByteBuffer.allocate(5 + values.map(_.length + 4).sum)
    buffer.put(5.toByte)
    buffer.putInt(values.length)
    values.foreach { value =>
      val bytes = value.getBytes
      buffer.putInt(bytes.length)
      buffer.put(bytes)
    }
    buffer.array()
  }

  private def serializeError(message: String): Array[Byte] = {
    val bytes = message.getBytes
    val buffer = ByteBuffer.allocate(5 + bytes.length)
    buffer.put(1.toByte)
    buffer.putInt(bytes.length)
    buffer.put(bytes)
    buffer.array()
  }
}
