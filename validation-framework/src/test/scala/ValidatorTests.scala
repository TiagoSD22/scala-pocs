import org.scalatest.funsuite.AnyFunSuite

class ValidatorTests extends AnyFunSuite {

  test("EmailValidator should validate correct email format") {
    assert(EmailValidator.validate("test@example.com"))
    assert(EmailValidator.validate("user.name+tag+sorting@example.com"))
  }

}