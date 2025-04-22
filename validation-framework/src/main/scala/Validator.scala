trait Validator[T] {
  def validate(value: T): Boolean
  def errorMessage: String
}

object EmailValidator extends Validator[String] {
  private val emailRegex = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$".r

  override def validate(value: String): Boolean = emailRegex.matches(value)

  override def errorMessage: String = "Invalid email format"
}