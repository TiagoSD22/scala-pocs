package validators

object NonEmptyValidator extends Validator[String] {
  override def validate(value: String): Boolean = value.nonEmpty

  override def errorMessage: String = "Field cannot be empty"
}