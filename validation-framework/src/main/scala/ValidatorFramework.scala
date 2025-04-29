import scala.deriving.Mirror
import scala.compiletime.{constValueTuple, erasedValue, summonInline}

object ValidatorFramework {
  inline def validate[T](entity: T): List[String] = {
    inline erasedValue[T] match {
      case _: Mirror.ProductOf[T] =>
        val mirror = summonInline[Mirror.ProductOf[T]]
        validateFields(entity, mirror)
      case _ => List.empty
    }
  }

  private inline def validateFields[T](entity: T, mirror: Mirror.ProductOf[T]): List[String] = {
    val fieldNames = constValueTuple[mirror.MirroredElemLabels].toList
    val fieldValues = entity.asInstanceOf[Product].productIterator.toList
    val fieldAnnotations = summonFieldAnnotations[mirror.MirroredElemTypes]

    fieldNames.zip(fieldValues).zip(fieldAnnotations).flatMap {
      case ((name, value), Some(Email())) =>
        if (!EmailValidator.validate(value.toString)) Some(s"Field '$name': ${EmailValidator.errorMessage}")
        else None
      case ((name, value), Some(NonEmpty())) =>
        if (!NonEmptyValidator.validate(value.toString)) Some(s"Field '$name': ${NonEmptyValidator.errorMessage}")
        else None
      case ((name, value), Some(Positive())) =>
        if (!PositiveValidator.validate(value.asInstanceOf[Int])) Some(s"Field '$name': ${PositiveValidator.errorMessage}")
        else None
      case _ => None
    }
  }

  private inline def summonFieldAnnotations[T <: Tuple]: List[Option[StaticAnnotation]] = inline erasedValue[T] match {
    case _: (t *: ts) =>
      summonInline[Option[StaticAnnotation]] :: summonFieldAnnotations[ts]
    case _ => Nil
  }
}