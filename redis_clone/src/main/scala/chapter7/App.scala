package chapter7

object App {
  def main(args: Array[String]): Unit = {
    val port = 1234
    val server = new Server(port)
    server.start()

    val client = new Client("127.0.0.1", port)

    try {
      val cmd = args.toList
      if (client.sendReq(cmd) != 0) {
        println("Error sending request")
        return
      }
      if (client.readRes() != 0) {
        println("Error reading response")
        return
      }
    } finally {
      client.close()
    }
  }
}