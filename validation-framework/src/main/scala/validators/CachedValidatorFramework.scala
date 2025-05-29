package validators

import scala.annotation.StaticAnnotation
import scala.collection.mutable
import scala.compiletime.{constValueTuple, erasedValue, summonInline}
import scala.deriving.Mirror
import java.lang.reflect.Field

object CachedValidatorFramework {
  // Cache for field validation metadata
  private val cache = mutable.Map[Class[_], List[(String, Field, Validator[Any])]]()

  def validate[T](entity: T): List[String] = {
    val entityClass = entity.getClass

    // Get validation metadata from cache or compute it
    val validationMetadata = cache.getOrElseUpdate(entityClass, {
      println(s"[REFLECTION] Computing metadata for class: ${entityClass.getName}")
      extractValidationMetadata(entityClass)
    })

    // Perform validation using cached metadata
    var errors = List.empty[String]

    for ((fieldName, field, validator) <- validationMetadata) {
      try {
        field.setAccessible(true)
        val fieldValue = field.get(entity)

        if (!validator.validate(fieldValue)) {
          errors = s"Field '$fieldName': ${validator.errorMessage}" :: errors
        }
      } catch {
        case e: Exception =>
          errors = s"Field '$fieldName': validation error - ${e.getMessage}" :: errors
      }
    }

    if (errors.isEmpty) List.empty else errors.reverse
  }

  private def extractValidationMetadata(clazz: Class[_]): List[(String, Field, Validator[Any])] = {
    val fields = clazz.getDeclaredFields

    fields.flatMap { field =>
      val fieldName = field.getName
      val validators = findValidators(field)

      validators.map(validator => (fieldName, field, validator))
    }.toList
  }

  private def findValidators(field: Field): List[Validator[Any]] = {
    val annotations = field.getDeclaredAnnotations

    annotations.flatMap { annotation =>
      // Get the annotation class
      val annotationClass = annotation.annotationType()

      // Check if this annotation is associated with a validator
      // We're looking for a static method or field in the annotation class
      // that returns a Validator instance
      try {
        // Try to get a validator from the annotation
        val validatorMethod = annotationClass.getDeclaredMethod("validator")
        validatorMethod.setAccessible(true)
        val validator = validatorMethod.invoke(annotation)

        if (validator.isInstanceOf[Validator[_]]) {
          Some(validator.asInstanceOf[Validator[Any]])
        } else None
      } catch {
        case _: Exception =>
          // If no validator method exists, check if annotation class itself is a Validator
          if (classOf[Validator[_]].isAssignableFrom(annotationClass)) {
            Some(annotation.asInstanceOf[Validator[Any]])
          } else None
      }
    }.toList
  }
}