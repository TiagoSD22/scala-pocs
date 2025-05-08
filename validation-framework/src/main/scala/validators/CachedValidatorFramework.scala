package validators

import annotations.{Email, NonEmpty, Positive}
import models.Product

import scala.annotation.StaticAnnotation
import scala.collection.mutable
import scala.compiletime.{constValueTuple, erasedValue, summonInline}
import scala.deriving.Mirror

object CachedValidatorFramework {
  // Modified to store only field name and annotation
  private val cache = mutable.Map[String, List[(String, Option[StaticAnnotation])]]()

  inline def validate[T](entity: T): List[String] = {
    val entityType = entity.getClass.getName
    val fieldMetadata = cache.getOrElseUpdate(entityType, extractFieldMetadata(entity))

    // Get current field values
    val fieldValues = entity.asInstanceOf[Product].productIterator.toList

    // Combine metadata with current values
    fieldMetadata.zip(fieldValues).flatMap {
      case ((name, Some(Email())), value) =>
        if (!EmailValidator.validate(value.toString)) Some(s"Field '$name': ${EmailValidator.errorMessage}")
        else None
      case ((name, Some(NonEmpty())), value) =>
        if (!NonEmptyValidator.validate(value.toString)) Some(s"Field '$name': ${NonEmptyValidator.errorMessage}")
        else None
      case ((name, Some(Positive())), value) =>
        if (!PositiveValidator.validate(value.asInstanceOf[Int])) Some(s"Field '$name': ${PositiveValidator.errorMessage}")
        else None
      case _ => None
    }
  }

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