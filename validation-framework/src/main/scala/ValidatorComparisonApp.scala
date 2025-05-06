package app

import validators.{CachedValidatorFramework, ValidatorFramework}
import annotations.{Email, NonEmpty, Positive}
import scala.util.Random

case class TestEntity(
                       @Email email: String,
                       @NonEmpty name: String,
                       @Positive age: Int
                     )

object ValidatorComparisonApp {

  def randomEmail(): String = {
    val domains = List("example.com", "test.org", "sample.net")
    s"user${Random.nextInt(10000)}@${domains(Random.nextInt(domains.length))}"
  }

  def randomName(): String = {
    val names = List("John", "Jane", "Alice", "Bob", "Charlie")
    names(Random.nextInt(names.length))
  }

  
}