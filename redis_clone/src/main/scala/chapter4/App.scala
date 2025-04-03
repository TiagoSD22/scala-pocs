package chapter4

import scala.concurrent.{ExecutionContext, Future}

object App {
  def main(args: Array[String]): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.global

    val server = new Server(1234)
    Future {
      server.start()
    }

    Thread.sleep(1000) // Wait for the server to start

    val client = new Client("127.0.0.1", 1234)
    client.query("hello1")
    client.query("hello2")
    client.query("hello3")
  }
}
