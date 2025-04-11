package chapter9

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import scala.util.{Failure, Success, Try}

class Client(address: String, port: Int) {
  private val kMaxMsg = 4096
  private val channel = SocketChannel.open(new InetSocketAddress(address, port))

  def sendRequest(cmd: List[String]): Int = {
    val len = cmd.foldLeft(4)(_ + 4 + _.getBytes.length)
    if (len > kMaxMsg) return -1

    val buffer = ByteBuffer.allocate(4 + len)
    buffer.putInt(len)
    buffer.putInt(cmd.size)
    cmd.foreach { s =>
      val bytes = s.getBytes
      buffer.putInt(bytes.length)
      buffer.put(bytes)
    }
    buffer.flip()
    writeAll(buffer)
  }

  def readResponse(): Int = {
    val header = ByteBuffer.allocate(4)
    if (readFull(header) < 0) return -1
    header.flip()
    val len = header.getInt
    if (len > kMaxMsg) {
      println("Response too long")
      return -1
    }

    val body = ByteBuffer.allocate(len)
    if (readFull(body) < 0) return -1
    body.flip()
    val data = new Array[Byte](len)
    body.get(data)
    printResponse(data)
  }

  private def writeAll(buffer: ByteBuffer): Int = {
    while (buffer.hasRemaining) {
      if (channel.write(buffer) <= 0) return -1
    }
    0
  }

  private def readFull(buffer: ByteBuffer): Int = {
    while (buffer.hasRemaining) {
      if (channel.read(buffer) <= 0) return -1
    }
    0
  }

  private def printResponse(data: Array[Byte]): Int = {
    if (data.isEmpty) {
      println("Bad response")
      return -1
    }
    data(0) match {
      case 0 => // TAG_NIL
        println("(nil)")
        1
      case 1 => // TAG_ERR
        if (data.length < 9) {
          println("Bad response")
          return -1
        }
        val code = ByteBuffer.wrap(data.slice(1, 5)).getInt
        val len = ByteBuffer.wrap(data.slice(5, 9)).getInt
        if (data.length < 9 + len) {
          println("Bad response")
          return -1
        }
        val msg = new String(data.slice(9, 9 + len))
        println(s"(err) $code $msg")
        9 + len
      case 2 => // TAG_STR
        if (data.length < 5) {
          println("Bad response")
          return -1
        }
        val len = ByteBuffer.wrap(data.slice(1, 5)).getInt
        if (data.length < 5 + len) {
          println("Bad response")
          return -1
        }
        val str = new String(data.slice(5, 5 + len))
        println(s"(str) $str")
        5 + len
      case 3 => // TAG_INT
        if (data.length < 9) {
          println("Bad response")
          return -1
        }
        val value = ByteBuffer.wrap(data.slice(1, 9)).getLong
        println(s"(int) $value")
        9
      case 4 => // TAG_DBL
        if (data.length < 9) {
          println("Bad response")
          return -1
        }
        val value = ByteBuffer.wrap(data.slice(1, 9)).getDouble
        println(s"(dbl) $value")
        9
      case 5 => // TAG_ARR
        if (data.length < 5) {
          println("Bad response")
          return -1
        }
        val len = ByteBuffer.wrap(data.slice(1, 5)).getInt
        println(s"(arr) len=$len")
        var offset = 5
        for (_ <- 0 until len) {
          val rv = printResponse(data.slice(offset, data.length))
          if (rv < 0) return rv
          offset += rv
        }
        println("(arr) end")
        offset
      case _ =>
        println("Bad response")
        -1
    }
  }

  def close(): Unit = channel.close()
}
