object PositiveValidator extends Validator[Int] {
  override def validate(value: Int): Boolean = value > 0

  override def errorMessage: String = "Value must be positive"
}