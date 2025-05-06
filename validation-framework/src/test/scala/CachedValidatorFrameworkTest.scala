import org.scalatest.funsuite.AnyFunSuite
import annotations.{Email, NonEmpty, Positive}
import validators.CachedValidatorFramework

case class TestEntity(
                       @Email email: String,
                       @NonEmpty name: String,
                       @Positive age: Int
                     )

class CachedValidatorFrameworkTest extends AnyFunSuite {


}