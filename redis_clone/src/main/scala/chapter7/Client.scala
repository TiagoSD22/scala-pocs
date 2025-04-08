package chapter7

import java.net.{InetAddress, Socket}
import java.io.{BufferedInputStream, BufferedOutputStream, DataInputStream, DataOutputStream}
import scala.util.{Failure, Success, Try}

class Client(address: String, port: Int) {
  private val socket = new Socket(InetAddress.getByName(address), port)
  private val in = new DataInputStream(new BufferedInputStream(socket.getInputStream))
  private val out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream))

  def sendReq(cmd: List[String]): Int = {
    val len = cmd.foldLeft(4)(_ + 4 + _.getBytes.length)
    if (len > Client.kMaxMsg) {
      return -1
    }

    val buffer = new Array[Byte](4 + len)
    val lenBytes = intToBytes(len)
    System.arraycopy(lenBytes, 0, buffer, 0, 4)

    val cmdCount = cmd.length
    val cmdCountBytes = intToBytes(cmdCount)
    System.arraycopy(cmdCountBytes, 0, buffer, 4, 4)

    var offset = 8
    cmd.foreach { s =>
      val strBytes = s.getBytes
      val strLenBytes = intToBytes(strBytes.length)
      System.arraycopy(strLenBytes, 0, buffer, offset, 4)
      System.arraycopy(strBytes, 0, buffer, offset + 4, strBytes.length)
      offset += 4 + strBytes.length
    }

    Try(out.write(buffer)) match {
      case Success(_) =>
        out.flush()
        0
      case Failure(_) => -1
    }
  }

  def readRes(): Int = {
    val header = new Array[Byte](4)
    if (readFull(header) != 0) {
      return -1
    }

    val len = bytesToInt(header)
    if (len > Client.kMaxMsg) {
      println("Message too long")
      return -1
    }

    val body = new Array[Byte](len)
    if (readFull(body) != 0) {
      return -1
    }

    if (len < 4) {
      println("Bad response")
      return -1
    }

    val resCode = bytesToInt(body.take(4))
    val message = new String(body.drop(4))
    println(s"Server says: [$resCode] $message")
    0
  }

  private def readFull(buffer: Array[Byte]): Int = {
    var n = buffer.length
    var offset = 0
    while (n > 0) {
      val bytesRead = in.read(buffer, offset, n)
      if (bytesRead <= 0) {
        return -1
      }
      n -= bytesRead
      offset += bytesRead
    }
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

  def close(): Unit = socket.close()
}

object Client {
  val kMaxMsg = 4096
}