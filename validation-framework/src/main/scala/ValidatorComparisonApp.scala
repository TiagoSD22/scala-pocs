package app

import validators.{CachedValidatorFramework, ValidatorFramework}
import models.Product
import scala.util.Random

object ValidatorComparisonApp {

  def randomPrice(): Int = Random.between(-10, 1000)

  def randomName(): String = {
    val names = List("Laptop", "Phone", "Tablet", "Watch", "Headphones")
    names(Random.nextInt(names.length))
  }

  def randomProduct(): Product = {
    Product(randomPrice(), randomName())
  }

  def main(args: Array[String]): Unit = {
    val entityCount = 100000
    val products = (1 to entityCount).map(_ => randomProduct())

    // Measure performance of ValidatorFramework
    val startValidatorFramework = System.nanoTime()
    val validatorFrameworkErrors = products.flatMap(product => ValidatorFramework.validate(product))
    val endValidatorFramework = System.nanoTime()

    // Measure performance of CachedValidatorFramework
    val startCachedValidatorFramework = System.nanoTime()
    val cachedValidatorFrameworkErrors = products.flatMap(product => CachedValidatorFramework.validate(product))
    val endCachedValidatorFramework = System.nanoTime()

    println(s"ValidatorFramework validated $entityCount products in ${(endValidatorFramework - startValidatorFramework) / 1e9} seconds")
    println(s"CachedValidatorFramework validated $entityCount products in ${(endCachedValidatorFramework - startCachedValidatorFramework) / 1e9} seconds")

    println(s"ValidatorFramework errors: ${validatorFrameworkErrors.size}")
    println(s"CachedValidatorFramework errors: ${cachedValidatorFrameworkErrors.size}")
  }
}