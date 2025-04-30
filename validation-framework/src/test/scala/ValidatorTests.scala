import org.scalatest.funsuite.AnyFunSuite
import validators.{EmailValidator, NonEmptyValidator, PositiveValidator}


class ValidatorTests extends AnyFunSuite {

  test("EmailValidator should validate correct email format") {
    assert(EmailValidator.validate("test@example.com"))
  }

  test("EmailValidator should invalidate incorrect email format") {
    assert(!EmailValidator.validate("plainaddress"))
    assert(!EmailValidator.validate("missing@domain"))
    assert(!EmailValidator.validate("missing.domain@"))
    assert(!EmailValidator.validate("missing@.com"))
  }

  test("EmailValidator should return correct error message") {
    assert(EmailValidator.errorMessage == "Invalid email format")
  }

  test("NonEmptyValidator should validate non-empty strings") {
    assert(NonEmptyValidator.validate("Non-empty string"))
    assert(NonEmptyValidator.validate(" "))
  }

  test("NonEmptyValidator should invalidate empty strings") {
    assert(!NonEmptyValidator.validate(""))
  }

  test("NonEmptyValidator should return correct error message") {
    assert(NonEmptyValidator.errorMessage == "Field cannot be empty")
  }

  test("PositiveValidator should validate positive integers") {
    assert(PositiveValidator.validate(1))
    assert(PositiveValidator.validate(100))
    assert(PositiveValidator.validate(Int.MaxValue))
  }

  test("PositiveValidator should invalidate zero and negative integers") {
    assert(!PositiveValidator.validate(0))
    assert(!PositiveValidator.validate(-1))
    assert(!PositiveValidator.validate(-100))
    assert(!PositiveValidator.validate(Int.MinValue))
  }

  test("PositiveValidator should return correct error message") {
    assert(PositiveValidator.errorMessage == "Value must be positive")
  }
}