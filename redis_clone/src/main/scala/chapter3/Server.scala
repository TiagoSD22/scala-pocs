package chapter3

import java.net.{DatagramPacket, DatagramSocket, InetAddress, ServerSocket, Socket}
import java.io.{BufferedReader, InputStreamReader, PrintWriter}

class Server {
  private val protocol = "tcp" // or "udp"
  private val port = 8080
  private val address = "127.0.0.1"

  def start(): Unit = {
    protocol.toLowerCase match {
      case "tcp" => startTcpServer()
      case "udp" => startUdpServer()
      case _ => throw new IllegalArgumentException("Unsupported protocol: " + protocol)
    }
  }

  private def startTcpServer(): Unit = {
    val serverSocket = new ServerSocket(port, 50, address)
    println(s"TCP server started on ${address.getHostAddress}:$port")

    while (true) {
      val clientSocket = serverSocket.accept()
      new Thread(() => handleTcpClient(clientSocket)).start()
    }
  }

  private def handleTcpClient(clientSocket: Socket): Unit = {
    val in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream))
    val out = new PrintWriter(clientSocket.getOutputStream, true)
    val message = in.readLine()
    println(s"Received message: $message")
    out.println(s"ACK: $message")
    clientSocket.close()
  }

  private def startUdpServer(): Unit = {
    val socket = new DatagramSocket(port, address)
    println(s"UDP server started on ${address.getHostAddress}:$port")

    while (true) {
      val buffer = new Array[Byte](1024)
      val packet = new DatagramPacket(buffer, buffer.length)
      socket.receive(packet)
      val message = new String(packet.getData, 0, packet.getLength)
      println(s"Received message: $message")
      val ackMessage = s"ACK: $message".getBytes
      val ackPacket = new DatagramPacket(ackMessage, ackMessage.length, packet.getAddress, packet.getPort)
      socket.send(ackPacket)
    }
  }
}
