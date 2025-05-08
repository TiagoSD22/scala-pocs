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




}