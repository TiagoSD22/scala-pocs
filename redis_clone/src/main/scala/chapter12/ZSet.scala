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

  // Insert into the AVL tree
  private def treeInsert(node: ZNode): Unit = {
    var parent: Option[ZNode] = None
    var current = root

    while (current.isDefined) {
      parent = current
      current = if (node.score < current.get.score || (node.score == current.get.score && node.name < current.get.name)) {
        current.get.left
      } else {
        current.get.right
      }
    }

    node.parent = parent
    if (parent.isEmpty) {
      root = Some(node)
    } else if (node.score < parent.get.score || (node.score == parent.get.score && node.name < parent.get.name)) {
      parent.get.left = Some(node)
    } else {
      parent.get.right = Some(node)
    }

    // Fix AVL tree balance (not implemented here for brevity)
  }

  // Delete a node from the AVL tree
  private def treeDelete(node: ZNode): Unit = {
    // AVL tree deletion logic (not implemented here for brevity)
  }

  // Lookup a node by name
  def lookup(name: String): Option[ZNode] = hmap.get(name)

 
}
