package validators

import annotations.{Email, NonEmpty, Positive}

import scala.annotation.StaticAnnotation
import scala.collection.mutable
import scala.compiletime.{constValueTuple, erasedValue, summonInline}
import scala.deriving.Mirror

object CachedValidatorFramework {
  // Cache stores field name and annotation pairs
  private val cache = mutable.Map[Class[_], List[(String, Option[StaticAnnotation])]]()

  inline def validate[T](entity: T): List[String] = {
    // Use class object directly as key (faster than getName)
    val entityClass = entity.getClass
    val fieldMetadata = cache.getOrElseUpdate(entityClass, extractFieldMetadata(entity))

    // Get values directly without intermediate List creation
    val productEntity = entity.asInstanceOf[scala.Product]

    // Avoid creating intermediate collections with direct iteration
    var errors = List.empty[String]
    var i = 0
    while (i < fieldMetadata.length) {
      val (name, annotOpt) = fieldMetadata(i)
      val value = productEntity.productElement(i)

      annotOpt match {
        case Some(Email()) =>
          if (!EmailValidator.validate(value.toString))
            errors = s"Field '$name': ${EmailValidator.errorMessage}" :: errors
        case Some(NonEmpty()) =>
          if (!NonEmptyValidator.validate(value.toString))
            errors = s"Field '$name': ${NonEmptyValidator.errorMessage}" :: errors
        case Some(Positive()) =>
          if (!PositiveValidator.validate(value.asInstanceOf[Int]))
            errors = s"Field '$name': ${PositiveValidator.errorMessage}" :: errors
        case _ => // No validation needed
      }
      i += 1
    }

    errors.reverse
  }

  // Keep the metadata extraction methods as they were
  private inline def extractFieldMetadata[T](entity: T): List[(String, Option[StaticAnnotation])] = {
    inline erasedValue[T] match {
      case _: Mirror.ProductOf[T] =>
        val mirror = summonInline[Mirror.ProductOf[T]]
        val fieldNames = constValueTuple[mirror.MirroredElemLabels].toList.asInstanceOf[List[String]]
        val fieldAnnotations = summonFieldAnnotations[mirror.MirroredElemTypes]
        fieldNames.zip(fieldAnnotations)
      case _ => List.empty
    }
  }

  private inline def summonFieldAnnotations[T <: Tuple]: List[Option[StaticAnnotation]] = inline erasedValue[T] match {
    case _: (t *: ts) =>
      summonInline[Option[StaticAnnotation]] :: summonFieldAnnotations[ts]
    case _ => Nil
  }
}