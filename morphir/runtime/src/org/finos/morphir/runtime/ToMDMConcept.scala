package org.finos.morphir.runtime

import org.finos.morphir.ir.conversion

import org.finos.morphir.naming._
import org.finos.morphir.datamodel.{Concept, Label, EnumLabel}
import org.finos.morphir.ir.AccessControlled
import org.finos.morphir.ir.{Type => T}
import org.finos.morphir.ir.Type.{Type, UType}

import scala.collection.immutable.{Map, Set}
import org.finos.morphir.runtime.Distributions
import org.finos.morphir.datamodel.Concept.Defaults
import org.finos.morphir.runtime.Extractors.Types.*

trait ToMDMConcept[A] { self =>
  import ToMDMConcept.*
  def apply(distributions: Distributions, bindings: Map[Name, Concept] = Map.empty): ToMDMConceptEither

  final def concept(distributions: Distributions, bindings: Map[Name, Concept] = Map.empty): ToMDMConceptEither =
    apply(distributions, bindings)

  def as[B]: ToMDMConcept[B] = new ToMDMConcept[B] {
    override def apply(distributions: Distributions, bindings: Map[Name, Concept] = Map.empty): ToMDMConceptEither =
      self.apply(distributions, bindings)
  }
}

object ToMDMConcept {

  sealed trait NoEquivalentMDM         extends Throwable
  case class Unsupported(msg: String)  extends NoEquivalentMDM
  case class MissingAlias(msg: String) extends NoEquivalentMDM
  type ToMDMConceptEither = Either[NoEquivalentMDM, Concept]

  def apply[A](implicit toMDMConcept: ToMDMConcept[A]): ToMDMConcept[A] = toMDMConcept
  // def summon[A]: SummonPartiallyApplied[A]                              = new SummonPartiallyApplied[A]

  def toConceptConverter[A](f: => Concept): ToMDMConcept[A] = new ToMDMConcept[A] {
    def apply(distributions: Distributions, bindings: Map[Name, Concept] = Map.empty): ToMDMConceptEither = Right(f)
  }
  def toFailedConverter[A](f: => NoEquivalentMDM): ToMDMConcept[A] = new ToMDMConcept[A] {
    def apply(distributions: Distributions, bindings: Map[Name, Concept] = Map.empty): ToMDMConceptEither = Left(f)
  }

  implicit def uTypeToConcept(tpe: UType): ToMDMConcept[UType] =
    tpe match {
      case _: Type.ExtensibleRecord[_] => toFailedConverter(Unsupported("Extensible record type has no MDM equivalent"))
      case _: Type.Function[_]         => toFailedConverter(Unsupported("Functions do not exist within MDM"))
      case _: Type.Unit[_]             => toConceptConverter(Concept.Unit)
      case IntRef()                    => toConceptConverter(Concept.Int32)
      case Int16Ref()                  => toConceptConverter(Concept.Int16)
      case Int32Ref()                  => toConceptConverter(Concept.Int32)
      case Int64Ref()                  => toConceptConverter(Concept.Int64)
      case StringRef()                 => toConceptConverter(Concept.String)
      case BoolRef()                   => toConceptConverter(Concept.Boolean)
      case CharRef()                   => toConceptConverter(Concept.Char)
      case FloatRef()                  => toConceptConverter(Concept.Float)
      case DecimalRef()                => toConceptConverter(Concept.Decimal)
      case LocalDateRef()              => toConceptConverter(Concept.LocalDate)
      case LocalTimeRef()              => toConceptConverter(Concept.LocalTime)
      case OrderRef()                  => toConceptConverter(Concept.Order)
      case MonthRef()                  => toConceptConverter(Concept.Month)
      case DayOfWeekRef()              => toConceptConverter(Concept.DayOfWeek)
      case Type.Variable(_, name) =>
        new ToMDMConcept[UType] {
          def apply(distributions: Distributions, bindings: Map[Name, Concept] = Map.empty): ToMDMConceptEither =
            Right(bindings(name)) // TODO: Error this up nice
        }
      case ResultRef(errType, okType) =>
        new ToMDMConcept[UType] {
          def apply(distributions: Distributions, bindings: Map[Name, Concept] = Map.empty): ToMDMConceptEither = for {
            errConcept <- errType.concept(distributions, bindings)
            okConcept  <- okType.concept(distributions, bindings)
          } yield Concept.Result(errConcept, okConcept)
        }
      case MaybeRef(elementType) =>
        new ToMDMConcept[UType] {
          def apply(distributions: Distributions, bindings: Map[Name, Concept] = Map.empty): ToMDMConceptEither = for {
            elementConcept <- elementType.concept(distributions, bindings)
          } yield Concept.Optional(elementConcept)
        }
      case ListRef(elementType) =>
        new ToMDMConcept[UType] {
          def apply(distributions: Distributions, bindings: Map[Name, Concept] = Map.empty): ToMDMConceptEither = for {
            elementConcept <- elementType.concept(distributions, bindings)
          } yield Concept.List(elementConcept)
        }
      case SetRef(elementType) =>
        new ToMDMConcept[UType] {
          def apply(distributions: Distributions, bindings: Map[Name, Concept] = Map.empty): ToMDMConceptEither = for {
            elementConcept <- elementType.concept(distributions, bindings)
          } yield Concept.Set(elementConcept)
        }
      case DictRef(keyType, valueType) =>
        new ToMDMConcept[UType] {
          def apply(distributions: Distributions, bindings: Map[Name, Concept] = Map.empty): ToMDMConceptEither = for {
            keyConcept   <- keyType.concept(distributions, bindings)
            valueConcept <- valueType.concept(distributions, bindings)
          } yield Concept.Map(keyConcept, valueConcept)
        }

      case Type.Record(_, fields) =>
        new ToMDMConcept[UType] {
          def apply(distributions: Distributions, bindings: Map[Name, Concept] = Map.empty): ToMDMConceptEither = for {
            dataFields <-
              Defaults.collectAll(
                fields,
                field => field.data.concept(distributions, bindings).map((Label(field.name.toCamelCase), _))
              )
            res = Concept.Struct(dataFields)
          } yield res
        }
      case Type.Tuple(_, elements) =>
        new ToMDMConcept[UType] {
          def apply(distributions: Distributions, bindings: Map[Name, Concept] = Map.empty): ToMDMConceptEither = for {
            elementConcepts <- Defaults.collectAll(elements, elem => elem.concept(distributions, bindings))
          } yield Concept.Tuple(elementConcepts)
        }
      // case other => toFailedConverter(Unsupported(s"No converter for $other"))
      case Type.Reference(_, typeName, typeArgs) =>
        new ToMDMConcept[UType] {
          def apply(dists: Distributions, bindings: Map[Name, Concept] = Map.empty): ToMDMConceptEither = for {
            lookedUp <- dists.lookupTypeDefinition(typeName.packagePath, typeName.modulePath, typeName.localName)
              .left.map { _ =>
                val string = typeName.toString()
                throw new Exception(s"IDK why this only shows up this way, but $string is missing")
                MissingAlias(s"Not actually unsupported, $string just missing")
              }

            conceptArgs <- Defaults.collectAll(typeArgs, typeArg => typeArg.concept(dists, bindings))
            res <- lookedUp match {
              case T.Definition.TypeAlias(typeParams, expr) =>
                val newBindings = typeParams.zip(conceptArgs).toMap
                expr.concept(dists, newBindings) match {
                  case Right(Concept.Struct(fields)) => Right(Concept.Record(typeName, fields))
                  case Right(other)                  => Right(Concept.Alias(typeName, other))
                  case err                           => err
                }
              case T.Definition.CustomType(typeParams, AccessControlled(_, ctors)) =>
                val newBindings = typeParams.zip(conceptArgs).toMap

                val cases = Defaults.collectAll(
                  ctors.toMap.toList,
                  (caseName, args) =>
                    for {
                      argTuples <- Defaults.collectAll(
                        args.toList,
                        (argName, argType) =>
                          argType.concept(dists, newBindings).map((EnumLabel.Named(argName.toCamelCase), _))
                      )
                      conceptName: String = caseName.toTitleCase
                    } yield Concept.Enum.Case(Label(conceptName), argTuples.toList)
                )
                cases.map(Concept.Enum(typeName, _))
            }
          } yield res
        }
    }

  // final class SummonPartiallyApplied[A](private val dummy: Boolean = true) extends AnyVal {
  //   def withAttributesOf[Attribs](implicit toMDMConcept: ToMDMConcept[A, Attribs]): ToMDMConcept[A, Attribs] =
  //     toMDMConcept
  // }
}
