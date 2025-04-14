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

  def avlUpdate(node: AVLNode): Unit = {
    node.height = 1 + max(avlHeight(node.left), avlHeight(node.right))
    node.cnt = 1 + avlCnt(node.left) + avlCnt(node.right)
  }

  def avlHeight(node: AVLNode): Int = if (node != null) node.height else 0
  def avlCnt(node: AVLNode): Int = if (node != null) node.cnt else 0

  def rotLeft(node: AVLNode): AVLNode = {
    val parent = node.parent
    val newNode = node.right
    val inner = newNode.left

    node.right = inner
    if (inner != null) inner.parent = node

    newNode.parent = parent
    newNode.left = node
    node.parent = newNode

    avlUpdate(node)
    avlUpdate(newNode)
    newNode
  }

  def rotRight(node: AVLNode): AVLNode = {
    val parent = node.parent
    val newNode = node.left
    val inner = newNode.right

    node.left = inner
    if (inner != null) inner.parent = node

    newNode.parent = parent
    newNode.right = node
    node.parent = newNode

    avlUpdate(node)
    avlUpdate(newNode)
    newNode
  }

  def avlFixLeft(node: AVLNode): AVLNode = {
    if (avlHeight(node.left.left) < avlHeight(node.left.right)) {
      node.left = rotLeft(node.left)
    }
    rotRight(node)
  }

  def avlFixRight(node: AVLNode): AVLNode = {
    if (avlHeight(node.right.right) < avlHeight(node.right.left)) {
      node.right = rotRight(node.right)
    }
    rotLeft(node)
  }

}
