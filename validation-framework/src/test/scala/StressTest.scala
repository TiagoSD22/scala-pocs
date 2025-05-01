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

  def randomUser(): User = {
    User(email = randomEmail(), name = randomName())
  }

  test("Stress test with random users") {
    val userCount = 1000000 // Adjust the number of users for testing
    val users = (1 to userCount).map(_ => randomUser())

    val startTime = System.nanoTime()
    val errors = users.flatMap(user => ValidatorFramework.validate(user))
    val endTime = System.nanoTime()

    println(s"Validated $userCount users in ${(endTime - startTime) / 1e9} seconds")
    println(s"Total validation errors: ${errors.size}")
  }
}