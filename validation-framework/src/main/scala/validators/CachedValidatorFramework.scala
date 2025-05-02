package validators

import annotations.{Email, NonEmpty, Positive}
import models.Product

import scala.annotation.StaticAnnotation
import scala.collection.mutable
import scala.compiletime.{constValueTuple, erasedValue, summonInline}
import scala.deriving.Mirror

object CachedValidatorFramework {
  private val cache = mutable.Map[String, List[(String, Any, Option[StaticAnnotation])]]()



  private inline def summonFieldAnnotations[T <: Tuple]: List[Option[StaticAnnotation]] = inline erasedValue[T] match {
    case _: (t *: ts) =>
      summonInline[Option[StaticAnnotation]] :: summonFieldAnnotations[ts]
    case _ => Nil
  }
}