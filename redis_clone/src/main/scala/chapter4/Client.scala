package chapter4

import java.net.{InetAddress, Socket}
import java.io.{BufferedInputStream, BufferedOutputStream, DataInputStream, DataOutputStream}

class Client(address: String, port: Int) {
  private val kMaxMsg = 4096

  def query(text: String): Unit = {
    val socket = new Socket(InetAddress.getByName(address), port)
    val in = new DataInputStream(new BufferedInputStream(socket.getInputStream))
    val out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream))

    try {
      val messageBytes = text.getBytes
      val length = messageBytes.length
      if (length > kMaxMsg) {
        throw new IllegalArgumentException("Message too long")
      }

      out.writeInt(length)
      out.write(messageBytes)
      out.flush()

      val responseLength = in.readInt()
      if (responseLength > kMaxMsg) {
        throw new IllegalArgumentException("Response too long")
      }

      val responseBytes = new Array[Byte](responseLength)
      in.readFully(responseBytes)
      val response = new String(responseBytes)
      println(s"Server says: $response")
    } catch {
      case e: Exception => println(s"Error: ${e.getMessage}")
    } finally {
      socket.close()
    }
  }
}
