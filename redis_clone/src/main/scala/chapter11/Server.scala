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

