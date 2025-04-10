package chapter8

import scala.collection.mutable.ArrayBuffer

case class HNode(var next: Option[HNode] = None, var hcode: Long = 0)

