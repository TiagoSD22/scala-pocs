package validators

import annotations.{Email, NonEmpty, Positive}
import models.Product

import scala.annotation.StaticAnnotation
import scala.collection.mutable
import scala.compiletime.{constValueTuple, erasedValue, summonInline}
import scala.deriving.Mirror

object CachedValidatorFramework {
  private val cache = mutable.Map[String, List[(String, Any, Option[StaticAnnotation])]]()

  inline def validate[T](entity: T): List[String] = {


  private inline def extractFields[T](entity: T): List[(String, Any, Option[StaticAnnotation])] = {
    inline erasedValue[T] match {
      case _: Mirror.ProductOf[T] =>
        val mirror = summonInline[Mirror.ProductOf[T]]
        val fieldNames = constValueTuple[mirror.MirroredElemLabels].toList.asInstanceOf[List[String]]
        val fieldValues = entity.asInstanceOf[Product].productIterator.toList
        val fieldAnnotations = summonFieldAnnotations[mirror.MirroredElemTypes]
        fieldNames.zip(fieldValues).zip(fieldAnnotations).map {
          case ((name, value), annotation) => (name, value, annotation)
        }
      case _ => List.empty
    }
  }

  private inline def summonFieldAnnotations[T <: Tuple]: List[Option[StaticAnnotation]] = inline erasedValue[T] match {
    case _: (t *: ts) =>
      summonInline[Option[StaticAnnotation]] :: summonFieldAnnotations[ts]
    case _ => Nil
  }
}