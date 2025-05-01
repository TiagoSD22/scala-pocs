import org.scalatest.funsuite.AnyFunSuite
import validators.ValidatorFramework
import models.User
import scala.util.Random

class StressTest extends AnyFunSuite {

  def randomEmail(): String = {
    val domains = List("example.com", "test.org", "sample.net")
    s"user${Random.nextInt(10000)}@${domains(Random.nextInt(domains.length))}"
  }

  def randomName(): String = {
    val names = List("John", "Jane", "Alice", "Bob", "Charlie")
    names(Random.nextInt(names.length))
  }

}