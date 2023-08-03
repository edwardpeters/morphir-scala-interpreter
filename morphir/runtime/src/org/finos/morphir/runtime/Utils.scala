package org.finos.morphir.runtime
import org.finos.morphir.ir.{Type as T, Value as V}
import V.Value
import T.Type
import org.finos.morphir.ir.{Name, QName, FQName}
import org.finos.morphir.ir.Module.QualifiedModuleName
import org.finos.morphir.ir.distribution.Distribution.Library
import zio.Chunk

/**
 * This file
 */

object Utils {

  def specificationToType[TA](spec: V.Specification[TA]): Type[TA] =
    curryTypeFunction(spec.output, spec.inputs)

  def unCurryTypeFunction(curried: Type[Unit], args: List[Type[Unit]], lib : Library): Either[TypeError, Type[Unit]] =
    (curried, args) match {
      case (Type.Function(attributes, parameterType, returnType), head :: tail) =>
        for {
          _           <- typeCheck(parameterType, head)
          appliedType <- unCurryTypeFunction(returnType, tail, lib)
        } yield appliedType
      case (tpe, Nil)               => Right(tpe)
      case (Type.Reference(attributes, typeName, typeArgs), args) => {//TODO: USE ARGS!
        lib.lookupTypeSpecification(typeName.packagePath, typeName.modulePath, typeName.localName) match {
          case Some(T.Specification.TypeAliasSpecification(typeParams, expr)) => unCurryTypeFunction(expr, args, lib)
          case Some(other) => Left(UnsupportedType(s"Found type $other during uncurrying"))
          case None => Left(TypeNotFound(s"Could not find type ${typeName.toString} during uncurrying"))
        }
      }
      case (nonFunction, head :: _) => Left(TooManyArgs(s"Tried to apply argument $head to non-function $nonFunction"))
    }
  // TODO: Implement
  def typeCheck[TA](t1: Type[TA], t2: Type[TA]): Either[TypeError, Unit] = Right(())
  def curryTypeFunction[TA](inner: Type[TA], params: Chunk[(Name, Type[TA])]): Type[TA] =
    params match {
      case Chunk() => inner
      case chunk =>
        curryTypeFunction(Type.Function(getAttributes(inner), chunk.head._2, inner), chunk.tail)
    }

  def getAttributes[TA](tpe: Type[TA]): TA =
    tpe match {
      case Type.ExtensibleRecord(attributes, _, _) => attributes
      case Type.Function(attributes, _, _)         => attributes
      case Type.Record(attributes, _)              => attributes
      case Type.Reference(attributes, _, _)        => attributes // TODO: Ignored type arguments here might be an issue
      case Type.Tuple(attributes, _)               => attributes
      case Type.Unit(attributes)                   => attributes
      case Type.Variable(attributes, _)            => attributes
    }

}
