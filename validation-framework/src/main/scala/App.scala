import models.User
import validators.ValidatorFramework

@main def runValidation(): Unit = {
  val user = User(email = "invalid-email", name = "John Doe")
  val errors = ValidatorFramework.validate(user)

  if (errors.nonEmpty) {
    println("Validation errors:")
    errors.foreach(println)
  } else {
    println("Validation passed!")
  }
}
