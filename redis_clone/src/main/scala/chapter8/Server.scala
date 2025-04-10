package chapter8

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
    println(s"Server started on port $port")
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
    val conn = new Connection(clientChannel)
    connections(clientChannel.hashCode()) = conn
    clientChannel.register(selector, SelectionKey.OP_READ, conn)
    println(s"Accepted connection from ${clientChannel.getRemoteAddress}")
  }

  private def handleRead(key: SelectionKey): Unit = {
    val clientChannel = key.channel().asInstanceOf[SocketChannel]
    val conn = key.attachment().asInstanceOf[Connection]
    val buffer = ByteBuffer.allocate(64 * 1024)
    val bytesRead = clientChannel.read(buffer)
    if (bytesRead == -1) {
      clientChannel.close()
      connections.remove(clientChannel.hashCode())
      println("Client closed connection")
    } else {
      buffer.flip()
      conn.incoming ++= buffer.array().take(bytesRead)
      while (tryOneRequest(conn)) {}
      if (conn.outgoing.nonEmpty) {
        key.interestOps(SelectionKey.OP_WRITE)
      }
    }
  }

  private def handleWrite(key: SelectionKey): Unit = {
    val clientChannel = key.channel().asInstanceOf[SocketChannel]
    val conn = key.attachment().asInstanceOf[Connection]
    val buffer = ByteBuffer.wrap(conn.outgoing.toArray)
    val bytesWritten = clientChannel.write(buffer)
    conn.outgoing = conn.outgoing.drop(bytesWritten)
    if (conn.outgoing.isEmpty) {
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
    val request = conn.incoming.slice(4, 4 + len)
    val cmd = parseRequest(request.toArray)
    cmd match {
      case Some(commands) =>
        val response = doRequest(commands)
        makeResponse(response, conn.outgoing)
        conn.incoming = conn.incoming.drop(4 + len)
        true
      case None =>
        println("Bad request")
        conn.wantClose = true
        false
    }
  }

  private def parseRequest(data: Array[Byte]): Option[List[String]] = {
    val buffer = ByteBuffer.wrap(data)
    Try {
      val nstr = buffer.getInt
      if (nstr > 200 * 1000) throw new IllegalArgumentException("Too many arguments")
      (0 until nstr).map { _ =>
        val len = buffer.getInt
        val strBytes = new Array[Byte](len)
        buffer.get(strBytes)
        new String(strBytes)
      }.toList
    }.toOption
  }

  private def doRequest(cmd: List[String]): Response = {
    cmd match {
      case "get" :: key :: Nil =>
        dataStore.get(key) match {
          case Some(value) => Response(0, value.getBytes.toSeq)
          case None        => Response(2, Seq.empty)
        }
      case "set" :: key :: value :: Nil =>
        dataStore.put(key, value)
        Response(0, Seq.empty)
      case "del" :: key :: Nil =>
        dataStore.remove(key)
        Response(0, Seq.empty)
      case _ =>
        Response(1, Seq.empty)
    }
  }

  private def makeResponse(resp: Response, out: mutable.ArrayBuffer[Byte]): Unit = {
    val buffer = ByteBuffer.allocate(4 + 4 + resp.data.length)
    buffer.putInt(4 + resp.data.length)
    buffer.putInt(resp.status)
    buffer.put(resp.data.toArray)
    out ++= buffer.array()
  }
}

case class Response(status: Int, data: Seq[Byte])