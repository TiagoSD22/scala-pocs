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

  def randomAge(): Int = Random.between(-10, 100)

  def randomEntity(): TestEntity = {
    TestEntity(randomEmail(), randomName(), randomAge())
  }

  def main(args: Array[String]): Unit = {
    val entityCount = 100000
    val entities = (1 to entityCount).map(_ => randomEntity())

    // Measure performance of ValidatorFramework
    val startValidatorFramework = System.nanoTime()
    val validatorFrameworkErrors = entities.flatMap(entity => ValidatorFramework.validate(entity))
    val endValidatorFramework = System.nanoTime()

    // Measure performance of CachedValidatorFramework
    val startCachedValidatorFramework = System.nanoTime()
    val cachedValidatorFrameworkErrors = entities.flatMap(entity => CachedValidatorFramework.validate(entity))
    val endCachedValidatorFramework = System.nanoTime()

    println(s"ValidatorFramework validated $entityCount entities in ${(endValidatorFramework - startValidatorFramework) / 1e9} seconds")
    println(s"CachedValidatorFramework validated $entityCount entities in ${(endCachedValidatorFramework - startCachedValidatorFramework) / 1e9} seconds")

    println(s"ValidatorFramework errors: ${validatorFrameworkErrors.size}")
    println(s"CachedValidatorFramework errors: ${cachedValidatorFrameworkErrors.size}")
  }
}