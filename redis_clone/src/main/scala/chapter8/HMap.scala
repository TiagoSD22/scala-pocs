package chapter8

import scala.collection.mutable.ArrayBuffer

case class HNode(var next: Option[HNode] = None, var hcode: Long = 0)

class HTab(var tab: Array[Option[HNode]] = Array.fill(0)(None), var mask: Int = 0, var size: Int = 0) {
  def hInit(n: Int): Unit = {
    require(n > 0 && ((n - 1) & n) == 0, "n must be a power of 2")
    tab = Array.fill(n)(None)
    mask = n - 1
    size = 0
  }

  def hInsert(node: HNode): Unit = {
    val pos = (node.hcode & mask).toInt
    node.next = tab(pos)
    tab(pos) = Some(node)
    size += 1
  }

  def hLookup(key: HNode, eq: (HNode, HNode) => Boolean): Option[HNode] = {
    if (tab.isEmpty) return None
    val pos = (key.hcode & mask).toInt
    var current = tab(pos)
    while (current.isDefined) {
      if (current.get.hcode == key.hcode && eq(current.get, key)) return current
      current = current.get.next
    }
    None
  }

  def hDetach(from: Option[HNode]): Option[HNode] = {
    from match {
      case Some(node) =>
        tab((node.hcode & mask).toInt) = node.next
        size -= 1
        Some(node)
      case None => None
    }
  }
}

