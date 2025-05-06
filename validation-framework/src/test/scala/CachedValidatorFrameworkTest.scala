import org.scalatest.funsuite.AnyFunSuite
import annotations.{Email, NonEmpty, Positive}
import validators.CachedValidatorFramework

case class TestEntity(
                       @Email email: String,
                       @NonEmpty name: String,
                       @Positive age: Int
                     )

class CachedValidatorFrameworkTest extends AnyFunSuite {

  test("validate should return no errors for valid entity") {
    val entity = TestEntity("test@example.com", "John Doe", 25)
    val errors = CachedValidatorFramework.validate(entity)
    assert(errors.isEmpty)
  }

  test("validate should return error for invalid email") {
    val entity = TestEntity("invalid-email", "John Doe", 25)
    val errors = CachedValidatorFramework.validate(entity)
    assert(errors.contains("Field 'email': Invalid email format"))
  }

  test("validate should return error for empty name") {
    val entity = TestEntity("test@example.com", "", 25)
    val errors = CachedValidatorFramework.validate(entity)
    assert(errors.contains("Field 'name': Field cannot be empty"))
  }

  
}