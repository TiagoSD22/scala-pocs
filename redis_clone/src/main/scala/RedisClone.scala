import scala.collection.mutable

class RedisClone {
  private val store = mutable.Map[String, String]()

  def set(key: String, value: String): Unit = {
    store(key) = value
  }

  def get(key: String): Option[String] = {
    store.get(key)
  }

  def remove(key: String): Option[String] = {
    store.remove(key)
  }

  def append(key: String, value: String): Option[String] = {
    store.get(key) match {
      case Some(existingValue) =>
        val newValue = existingValue + value
        store(key) = newValue
        Some(newValue)
      case None =>
        None
    }
  }
}
