package chapter3

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Failure, Success}

object App {
  def main(args: Array[String]): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.global

    val protocol = "tcp" // or udp
    val server = new Server(protocol)

    Future {
      server.start()
    }

    Thread.sleep(1000) // Wait for the server to start

    val client = new Client(protocol)
    client.sendMessage("hello!")
  }
}
