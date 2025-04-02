package chapter3

import java.net.{DatagramPacket, DatagramSocket, InetAddress, Socket}
import java.io.{BufferedReader, InputStreamReader, PrintWriter}

class Client(private val protocol: String, private val port: Int = 8234, private val address: String = "127.0.0.1") {

  def sendMessage(message: String): Unit = {
    protocol.toLowerCase match {
      case "tcp" => sendTcpMessage(message)
      case "udp" => sendUdpMessage(message)
      case _ => throw new IllegalArgumentException("Unsupported protocol: " + protocol)
    }
  }

  private def sendTcpMessage(message: String): Unit = {
    val socket = new Socket(address, port)
    val out = new PrintWriter(socket.getOutputStream, true)
    val in = new BufferedReader(new InputStreamReader(socket.getInputStream))

    out.println(message)
    val response = in.readLine()
    println(s"Received response: $response")

    socket.close()
  }

  private def sendUdpMessage(message: String): Unit = {
    val socket = new DatagramSocket()
    val address = InetAddress.getByName(this.address)
    val buffer = message.getBytes
    val packet = new DatagramPacket(buffer, buffer.length, address, port)

    socket.send(packet)

    val responseBuffer = new Array[Byte](1024)
    val responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length)
    socket.receive(responsePacket)
    val response = new String(responsePacket.getData, 0, responsePacket.getLength)
    println(s"Received response: $response")

    socket.close()
  }
}
