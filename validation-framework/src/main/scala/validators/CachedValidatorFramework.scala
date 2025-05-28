package validators

import annotations.{Email, NonEmpty, Positive}

import scala.annotation.StaticAnnotation
import scala.collection.mutable
import scala.compiletime.{constValueTuple, erasedValue, summonInline}
import scala.deriving.Mirror
import java.lang.reflect.Field
import java.lang.annotation.Annotation

object CachedValidatorFramework {
  // Cache for field metadata
  private val cache = mutable.Map[Class[_], List[(String, Option[StaticAnnotation])]]()

  inline def validate[T](entity: T): List[String] = {
    val entityClass = entity.getClass

    // Get metadata from cache or compute it once
    val fieldMetadata = cache.getOrElseUpdate(entityClass, {
      println(s"[REFLECTION] Computing metadata for class: ${entityClass.getName}")
      inline erasedValue[T] match {
        case _: Mirror.ProductOf[T] =>
          val mirror = summonInline[Mirror.ProductOf[T]]
          val fieldNames = constValueTuple[mirror.MirroredElemLabels].toList.asInstanceOf[List[String]]
          val fieldAnnotations = summonFieldAnnotations[mirror.MirroredElemTypes]
          println(s"[REFLECTION] Found ${fieldNames.length} fields with annotations")
          fieldNames.zip(fieldAnnotations)
        case _ =>
          println(s"[REFLECTION] Falling back to Java reflection for class: ${entityClass.getName}")
          extractMetadataUsingJavaReflection(entityClass)
      }
    })

    validateEntity(entity, fieldMetadata)
  }

  private def validateEntity[T](entity: T, fieldMetadata: List[(String, Option[StaticAnnotation])]): List[String] = {
    if (entity.isInstanceOf[scala.Product]) {
      // Fast path for product types
      validateProductEntity(entity.asInstanceOf[scala.Product], fieldMetadata)
    } else {
      // Fallback for non-product types using Java reflection
      validateNonProductEntity(entity, fieldMetadata)
    }
  }

  private def validateProductEntity(entity: scala.Product, fieldMetadata: List[(String, Option[StaticAnnotation])]): List[String] = {
    var errors = List.empty[String]
    var i = 0
    val len = fieldMetadata.length

    while (i < len) {
      val (name, annotOpt) = fieldMetadata(i)
      val value = entity.productElement(i)

      errors = validateField(name, value, annotOpt, errors)
      i += 1
    }

    if (errors.isEmpty) List.empty else errors.reverse
  }

  private def validateNonProductEntity[T](entity: T, fieldMetadata: List[(String, Option[StaticAnnotation])]): List[String] = {
    var errors = List.empty[String]

    for ((name, annotOpt) <- fieldMetadata) {
      try {
        val field = entity.getClass.getDeclaredField(name)
        field.setAccessible(true)
        val value = field.get(entity)
        errors = validateField(name, value, annotOpt, errors)
      } catch {
        case e: Exception =>
          errors = s"Field '$name': reflection error - ${e.getMessage}" :: errors
      }
    }

    if (errors.isEmpty) List.empty else errors.reverse
  }

  private def validateField(name: String, value: Any, annotOpt: Option[StaticAnnotation], errors: List[String]): List[String] = {
    annotOpt match {
      case Some(Email()) if !EmailValidator.validate(value.toString) =>
        s"Field '$name': ${EmailValidator.errorMessage}" :: errors
      case Some(NonEmpty()) if !NonEmptyValidator.validate(value.toString) =>
        s"Field '$name': ${NonEmptyValidator.errorMessage}" :: errors
      case Some(Positive()) if !PositiveValidator.validate(value.asInstanceOf[Int]) =>
        s"Field '$name': ${PositiveValidator.errorMessage}" :: errors
      case _ => errors
    }
  }

  private inline def summonFieldAnnotations[T <: Tuple]: List[Option[StaticAnnotation]] = inline erasedValue[T] match {
    case _: (t *: ts) =>
      summonInline[Option[StaticAnnotation]] :: summonFieldAnnotations[ts]
    case _ => Nil
  }

  private def extractMetadataUsingJavaReflection(clazz: Class[_]): List[(String, Option[StaticAnnotation])] = {
    val fields = clazz.getDeclaredFields

    fields.map { field =>
      val name = field.getName
      val annotOpt = findValidationAnnotation(field)
      (name, annotOpt)
    }.toList
  }

  import scala.util.boundary
  import scala.util.boundary.break

  private def findValidationAnnotation(field: Field): Option[StaticAnnotation] = {
    // Get all annotations on the field
    val annotations = field.getDeclaredAnnotations

    boundary {
      // Look for our custom annotations by class name
      for (annotation <- annotations) {
        val annotationClass = annotation.annotationType().getName
        if (annotationClass.endsWith("Email")) {
          break(Some(Email()))
        } else if (annotationClass.endsWith("NonEmpty")) {
          break(Some(NonEmpty()))
        } else if (annotationClass.endsWith("Positive")) {
          break(Some(Positive()))
        }
      }

      None
    }
  }
}