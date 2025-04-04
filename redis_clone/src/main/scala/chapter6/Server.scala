package chapter6

import java.net.{InetSocketAddress, ServerSocket, Socket}
import java.nio.ByteBuffer
import java.nio.channels.{SelectionKey, Selector, ServerSocketChannel, SocketChannel}
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

object ServerApp {
  def main(args: Array[String]): Unit = {
    val server = new Server(1234)
    server.start()
  }
}

class Server(port: Int) {
  private val kMaxMsg = 32 << 20
  private val selector = Selector.open()
  private val serverChannel = ServerSocketChannel.open()
  serverChannel.bind(new InetSocketAddress(port))
  serverChannel.configureBlocking(false)
  serverChannel.register(selector, SelectionKey.OP_ACCEPT)

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
    clientChannel.register(selector, SelectionKey.OP_READ, new Connection(clientChannel))
    println(s"Accepted connection from ${clientChannel.getRemoteAddress}")
  }

  private def handleRead(key: SelectionKey): Unit = {
    val clientChannel = key.channel().asInstanceOf[SocketChannel]
    val conn = key.attachment().asInstanceOf[Connection]
    val buffer = ByteBuffer.allocate(64 * 1024)
    val bytesRead = clientChannel.read(buffer)
    if (bytesRead == -1) {
      clientChannel.close()
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
    val len = ByteBuffer.wrap(conn.incoming.take(4)).getInt
    if (len > kMaxMsg) {
      println("Message too long")
      conn.wantClose = true
      return false
    }
    if (conn.incoming.length < 4 + len) return false
    val request = conn.incoming.slice(4, 4 + len)
    println(s"Client says: len: $len data: ${new String(request.take(100))}")
    conn.outgoing ++= ByteBuffer.allocate(4).putInt(len).array()
    conn.outgoing ++= request
    conn.incoming = conn.incoming.drop(4 + len)
    true
  }
}

class Connection(val channel: SocketChannel) {
  var incoming: mutable.ArrayBuffer[Byte] = mutable.ArrayBuffer.empty
  var outgoing: mutable.ArrayBuffer[Byte] = mutable.ArrayBuffer.empty
  var wantClose: Boolean = false
}
