package chapter4

import java.net.{ServerSocket, Socket}
import java.io.{BufferedInputStream, BufferedOutputStream, DataInputStream, DataOutputStream}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class Server(port: Int) {
  implicit val ec: ExecutionContext = ExecutionContext.global

  def start(): Unit = {
    val serverSocket = new ServerSocket(port)
    println(s"[SERVER] - TCP server started on port $port")

    while (true) {
      val clientSocket = serverSocket.accept()
      Future {
        handleClient(clientSocket)
      }.onComplete {
        case Success(_) => println("[SERVER] - Client handled successfully")
        case Failure(e) => println(s"[SERVER] - Error handling client: ${e.getMessage}")
      }
    }
  }

  private def handleClient(clientSocket: Socket): Unit = {
    val in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream))
    val out = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream))

    try {
      while (true) {
        val length = in.readInt()
        if (length > 4096) {
          println("[SERVER] - Message too long")
          return
        }

        val messageBytes = new Array[Byte](length)
        in.readFully(messageBytes)
        val message = new String(messageBytes)
        println(s"[SERVER] - Received message: $message")

        val ackMessage = s"ACK: $message"
        val ackBytes = ackMessage.getBytes
        out.writeInt(ackBytes.length)
        out.write(ackBytes)
        out.flush()
      }
    } catch {
      case e: Exception => println(s"[SERVER] - Error: ${e.getMessage}")
    } finally {
      clientSocket.close()
    }
  }
}
