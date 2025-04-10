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

class HMap {
  private val kRehashingWork = 128
  private val kMaxLoadFactor = 8

  var newer: HTab = new HTab()
  var older: HTab = new HTab()
  var migratePos: Int = 0

  def hmHelpRehashing(): Unit = {
    var nwork = 0
    while (nwork < kRehashingWork && older.size > 0) {
      val from = older.tab(migratePos)
      if (from.isEmpty) {
        migratePos += 1
      } else {
        newer.hInsert(older.hDetach(from).get)
        nwork += 1
      }
    }
    if (older.size == 0 && older.tab.nonEmpty) {
      older = new HTab()
    }
  }

  def hmTriggerRehashing(): Unit = {
    require(older.tab.isEmpty, "Older table must be empty")
    older = newer
    newer = new HTab()
    newer.hInit((older.mask + 1) * 2)
    migratePos = 0
  }

  def hmLookup(key: HNode, eq: (HNode, HNode) => Boolean): Option[HNode] = {
    hmHelpRehashing()
    newer.hLookup(key, eq).orElse(older.hLookup(key, eq))
  }

  def hmInsert(node: HNode): Unit = {
    if (newer.tab.isEmpty) {
      newer.hInit(4)
    }
    newer.hInsert(node)
    if (older.tab.isEmpty) {
      val threshold = (newer.mask + 1) * kMaxLoadFactor
      if (newer.size >= threshold) {
        hmTriggerRehashing()
      }
    }
    hmHelpRehashing()
  }

  def hmDelete(key: HNode, eq: (HNode, HNode) => Boolean): Option[HNode] = {
    hmHelpRehashing()
    newer.hLookup(key, eq).flatMap(newer.hDetach).orElse(older.hLookup(key, eq).flatMap(older.hDetach))
  }

  def hmClear(): Unit = {
    newer = new HTab()
    older = new HTab()
    migratePos = 0
  }

  def hmSize: Int = newer.size + older.size
}