package chapter10

class AVLNode(var parent: AVLNode = null,
              var left: AVLNode = null,
              var right: AVLNode = null,
              var height: Int = 1,
              var cnt: Int = 1)

object AVLNode {
  private def max(lhs: Int, rhs: Int): Int = if (lhs < rhs) rhs else lhs

  def avlInit(node: AVLNode): Unit = {
    node.left = null
    node.right = null
    node.parent = null
    node.height = 1
    node.cnt = 1
  }
}
