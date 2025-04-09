package chapter6

import java.net.{InetAddress, Socket}
import java.io.{BufferedInputStream, BufferedOutputStream, DataInputStream, DataOutputStream}
import scala.util.{Failure, Success, Try}
import scala.math.Ordered.orderingToOrdered
import scala.math.Ordering.Implicits.infixOrderingOps

object ClientApp {
  def main(args: Array[String]): Unit = {
    val client = new Client("127.0.0.1", 1234)
    val queryList = List(
      "hello1", "hello2", "hello3",
      "z" * Client.kMaxMsg,
      "hello5"
    )

    queryList.foreach { query =>
      if (client.sendReq(query.getBytes) != 0) {
        println("Error sending request")
        return
      }
    }

    queryList.indices.foreach { _ =>
      if (client.readRes() != 0) {
        println("Error reading response")
        return
      }
    }
  }
}

class Client(address: String, port: Int) {
  import Client._

  private val socket = new Socket(InetAddress.getByName(address), port)
  private val in = new DataInputStream(new BufferedInputStream(socket.getInputStream))
  private val out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream))

  def sendReq(text: Array[Byte]): Int = {
    val len = text.length
    if (len > kMaxMsg) {
      return -1
    }

    val wbuf = new Array[Byte](4 + len)
    System.arraycopy(intToBytes(len), 0, wbuf, 0, 4)
    System.arraycopy(text, 0, wbuf, 4, len)
    writeAll(wbuf)
  }

  def readRes(): Int = {
    val rbuf = new Array[Byte](4)
    if (readFull(rbuf) != 0) {
      return -1
    }

    val len = bytesToInt(rbuf)
    if (len > kMaxMsg) {
      println("Message too long")
      return -1
    }

    val body = new Array[Byte](len)
    if (readFull(body) != 0) {
      return -1
    }

    println(s"len: $len data: ${new String(body.take(100))}")
    0
  }

  private def readFull(buf: Array[Byte]): Int = {
    var n = buf.length
    var offset = 0
    while (n > 0) {
      val rv = in.read(buf, offset, n)
      if (rv <= 0) {
        return -1
      }
      n -= rv
      offset += rv
    }
    0
  }

  private def writeAll(buf: Array[Byte]): Int = {
    var n = buf.length
    var offset = 0
    while (n > 0) {
      Try(out.write(buf, offset, n)) match {
        case Failure(_) => return -1
        case Success(_) =>
          n -= n // Adjust `n` manually since `out.write` does not return a value
          offset += n
      }
    }
    out.flush()
    0
  }

  private def intToBytes(value: Int): Array[Byte] = {
    Array(
      (value >> 24).toByte,
      (value >> 16).toByte,
      (value >> 8).toByte,
      value.toByte
    )
  }

  private def bytesToInt(bytes: Array[Byte]): Int = {
    (bytes(0) & 0xFF) << 24 |
      (bytes(1) & 0xFF) << 16 |
      (bytes(2) & 0xFF) << 8 |
      (bytes(3) & 0xFF)
  }
}

object Client {
  val kMaxMsg: Int = 32 << 20
}
