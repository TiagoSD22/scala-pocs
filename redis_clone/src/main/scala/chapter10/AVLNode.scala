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

  def avlFix(node: AVLNode): AVLNode = {
    var current = node
    while (true) {
      val parent = current.parent
      avlUpdate(current)

      val l = avlHeight(current.left)
      val r = avlHeight(current.right)

      if (l == r + 2) {
        current = avlFixLeft(current)
      } else if (l + 2 == r) {
        current = avlFixRight(current)
      }

      if (parent == null) return current
      current = parent
    }
    current
  }

  def avlDelEasy(node: AVLNode): AVLNode = {
    require(node.left == null || node.right == null, "Node must have at most one child")
    val child = if (node.left != null) node.left else node.right
    val parent = node.parent

    if (child != null) child.parent = parent

    if (parent == null) {
      child
    } else {
      if (parent.left == node) parent.left = child else parent.right = child
      avlFix(parent)
    }
  }

  def avlDel(node: AVLNode): AVLNode = {
    if (node.left == null || node.right == null) {
      return avlDelEasy(node)
    }

    var victim = node.right
    while (victim.left != null) {
      victim = victim.left
    }

    val root = avlDelEasy(victim)
    victim.left = node.left
    victim.right = node.right
    victim.parent = node.parent

    if (victim.left != null) victim.left.parent = victim
    if (victim.right != null) victim.right.parent = victim

    if (node.parent == null) {
      victim
    } else {
      if (node.parent.left == node) node.parent.left = victim else node.parent.right = victim
      root
    }
  }
}
