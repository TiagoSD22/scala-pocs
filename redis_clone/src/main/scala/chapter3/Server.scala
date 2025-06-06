package chapter3

import java.net.{DatagramPacket, DatagramSocket, InetAddress, ServerSocket, Socket}
import java.io.{BufferedReader, InputStreamReader, PrintWriter}

class Server(private val protocol: String, private val port: Int = 8234, private val address: String = "127.0.0.1") {

  def start(): Unit = {
    protocol.toLowerCase match {
      case "tcp" => startTcpServer()
      case "udp" => startUdpServer()
      case _ => throw new IllegalArgumentException("Unsupported protocol: " + protocol)
    }
  }

  private def startTcpServer(): Unit = {
    val serverSocket = new ServerSocket(port, 50, InetAddress.getByName(address))
    println(s"[SERVER] - TCP server started on ${address}:$port")

    while (true) {
      val clientSocket = serverSocket.accept()
      new Thread(() => handleTcpClient(clientSocket)).start()
    }
  }

  private def handleTcpClient(clientSocket: Socket): Unit = {
    val in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream))
    val out = new PrintWriter(clientSocket.getOutputStream, true)
    val message = in.readLine()
    println(s"[SERVER] - Received message: $message")
    out.println(s"ACK: $message")
    clientSocket.close()
  }

  private def startUdpServer(): Unit = {
    val socket = new DatagramSocket(port, InetAddress.getByName(address))
    println(s"[SERVER] - UDP server started on ${address}:$port")

    while (true) {
      val buffer = new Array[Byte](1024)
      val packet = new DatagramPacket(buffer, buffer.length)
      socket.receive(packet)
      val message = new String(packet.getData, 0, packet.getLength)
      println(s"[SERVER] - Received message: $message")
      val ackMessage = s"ACK: $message".getBytes
      val ackPacket = new DatagramPacket(ackMessage, ackMessage.length, packet.getAddress, packet.getPort)
      socket.send(ackPacket)
    }
  }
}