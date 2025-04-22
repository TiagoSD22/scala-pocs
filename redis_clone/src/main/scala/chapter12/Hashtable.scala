package chapter12

import scala.collection.mutable
import scala.annotation.tailrec

// Represents a node in the hashtable
case class HNode(hcode: Int, var next: Option[HNode] = None)

// Represents a hashtable
class HTab(initialSize: Int) {
  require((initialSize & (initialSize - 1)) == 0, "Size must be a power of 2")

  private val table: Array[Option[HNode]] = Array.fill(initialSize)(None)
  private val mask: Int = initialSize - 1
  var size: Int = 0

  def insert(node: HNode): Unit = {
    val pos = node.hcode & mask
    node.next = table(pos)
    table(pos) = Some(node)
    size += 1
  }

  def lookup(key: HNode, eq: (HNode, HNode) => Boolean): Option[HNode] = {
    val pos = key.hcode & mask
    @tailrec
    def find(node: Option[HNode]): Option[HNode] = node match {
      case Some(n) if n.hcode == key.hcode && eq(n, key) => node
      case Some(n) => find(n.next)
      case None => None
    }
    find(table(pos))
  }

  def detach(key: HNode, eq: (HNode, HNode) => Boolean): Option[HNode] = {
    val pos = key.hcode & mask
    @tailrec
    def findAndDetach(prev: Option[HNode], current: Option[HNode]): Option[HNode] = current match {
      case Some(n) if n.hcode == key.hcode && eq(n, key) =>
        prev match {
          case Some(p) => p.next = n.next
          case None => table(pos) = n.next
        }
        size -= 1
        Some(n)
      case Some(n) => findAndDetach(current, n.next)
      case None => None
    }
    findAndDetach(None, table(pos))
  }

  def foreach(f: HNode => Boolean): Boolean = {
    table.forall {
      case Some(node) =>
        @tailrec
        def traverse(n: Option[HNode]): Boolean = n match {
          case Some(current) => f(current) && traverse(current.next)
          case None => true
        }
        traverse(Some(node))
      case None => true
    }
  }
}

// Represents a hashmap with rehashing
class HMap {
  private val kRehashingWork = 128
  private val kMaxLoadFactor = 8

  private var newer = new HTab(4)
  private var older: Option[HTab] = None
  private var migratePos: Int = 0

  def insert(node: HNode): Unit = {
    newer.insert(node)
    if (older.isEmpty && newer.size >= (newer.mask + 1) * kMaxLoadFactor) {
      triggerRehashing()
    }
    helpRehashing()
  }

  def lookup(key: HNode, eq: (HNode, HNode) => Boolean): Option[HNode] = {
    helpRehashing()
    newer.lookup(key, eq).orElse(older.flatMap(_.lookup(key, eq)))
  }

  
}
