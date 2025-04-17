package chapter11

import java.net.{InetAddress, ServerSocket, Socket}
import java.nio.ByteBuffer
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success, Try}

// Buffer management
class Buffer {
  private val data = ArrayBuffer[Byte]()

  def append(bytes: Array[Byte]): Unit = data ++= bytes
  def consume(n: Int): Unit = data.remove(0, n)
  def size: Int = data.size
  def toArray: Array[Byte] = data.toArray
  def slice(start: Int, end: Int): Array[Byte] = data.slice(start, end).toArray
}

// Connection class
class Conn(val socket: Socket) {
  var wantRead: Boolean = true
  var wantWrite: Boolean = false
  var wantClose: Boolean = false
  val incoming: Buffer = new Buffer()
  val outgoing: Buffer = new Buffer()
}

// Utility functions
object Utils {
  def log(msg: String): Unit = println(msg)
  def logError(msg: String, ex: Throwable): Unit = println(s"Error: $msg - ${ex.getMessage}")
}

// Request parsing
def parseRequest(data: Array[Byte]): Try[List[String]] = Try {
  val buffer = ByteBuffer.wrap(data)
  val nstr = buffer.getInt
  (0 until nstr).map { _ =>
    val len = buffer.getInt
    val strBytes = new Array[Byte](len)
    buffer.get(strBytes)
    new String(strBytes)
  }.toList
}

// Example request handler
def handleRequest(cmd: List[String], conn: Conn): Unit = {
  cmd match {
    case "get" :: key :: Nil =>
      conn.outgoing.append(s"GET: $key".getBytes)
    case "set" :: key :: value :: Nil =>
      conn.outgoing.append(s"SET: $key -> $value".getBytes)
    case _ =>
      conn.outgoing.append("Unknown command".getBytes)
  }
}

