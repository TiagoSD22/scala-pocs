package chapter3

import scala.concurrent.{ExecutionContext, Future}

object App {
  def main(args: Array[String]): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.global

    val protocol = "tcp" // or udp
    val port = 8234
    val address = "127.0.0.1"

    val server = new Server(protocol, port, address)

    Future {
      server.start()
    }

    Thread.sleep(1000) // Wait for the server to start

    val client = new Client(protocol, port, address)
    client.sendMessage("hello!")
  }
}
