package chapter12

import scala.collection.mutable

// ZNode class
case class ZNode(name: String, var score: Double, var left: Option[ZNode] = None, var right: Option[ZNode] = None, var parent: Option[ZNode] = None)

// ZSet class
class ZSet {
  private val hmap = mutable.HashMap[String, ZNode]()
  private var root: Option[ZNode] = None

  // Insert or update a node
  def insert(name: String, score: Double): Boolean = {
    hmap.get(name) match {
      case Some(node) =>
        update(node, score)
        false
      case None =>
        val newNode = ZNode(name, score)
        hmap.put(name, newNode)
        treeInsert(newNode)
        true
    }
  }

  // Update the score of an existing node
  private def update(node: ZNode, score: Double): Unit = {
    if (node.score != score) {
      treeDelete(node)
      node.score = score
      treeInsert(node)
    }
  }

  
}
