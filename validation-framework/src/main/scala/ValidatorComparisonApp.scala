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


}