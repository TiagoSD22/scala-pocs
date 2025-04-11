package chapter9

object App {
  def main(args: Array[String]): Unit = {
    val server = new Server(1234)
    server.start()
    
    val client = new Client("127.0.0.1", 1234)
    try {
      val cmd = args.toList
      if (client.sendRequest(cmd) != 0) {
        println("Error sending request")
        return
      }
      if (client.readResponse() != 0) {
        println("Error reading response")
        return
      }
    } finally {
      client.close()
    }
  }
}
