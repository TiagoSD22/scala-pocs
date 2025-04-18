package chapter12

import scala.collection.mutable

// ZNode class
case class ZNode(name: String, var score: Double, var left: Option[ZNode] = None, var right: Option[ZNode] = None, var parent: Option[ZNode] = None)


