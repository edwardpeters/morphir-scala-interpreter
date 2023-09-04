package org.finos.morphir.runtime.lattices

import org.finos.morphir.naming._
import org.finos.morphir.ir.{Type as T, Value as V}
import org.finos.morphir.ir.Value.{Value, Pattern, RawValue, USpecification => UValueSpec}
import org.finos.morphir.ir.Type.{Type, UType, USpecification => UTypeSpec}
import org.finos.morphir.ir.Literal.Lit
import org.finos.morphir.ir.Literal.Literal.{
  BoolLiteral,
  CharLiteral,
  DecimalLiteral,
  FloatLiteral,
  StringLiteral,
  WholeNumberLiteral
}

object ProcessValue{
    def raw(value : RawValue, prefix : lpn) : List(Constraint)= {
        value match {
          case Literal((), lit)              => handleLiteral(prefix, lit)
          case Apply((), function, argument) => ???
          case Destructure((), pattern, valueToDestruct, inValue) =>???
          case Constructor((), name)             => ???
          case FieldValue((), recordValue, name) => ???
          case FieldFunction((), name)           => ???
          case IfThenElse((), condition, thenValue, elseValue) =>???
          case Lambda((), pattern, body) => ???
          case LetDefinition((), name, definition, inValue) =>???
          case LetRecursion((), definitions, inValue)  => ???
          case ListValue((), elements)                 => ???
          case PatternMatch((), value, cases)          => ???
          case Record((), fields)        => ???
          case Reference((), name)                     => ???
          case Tuple((), elements)                     => ???
          case UnitValue(())                           => ???
          case UpdateRecord((), valueToUpdate, fields) => ???
          case Variable((), name)                      => ???
        }
    }
    def handleLiteral(prefix : LPName, lit : Lit) : List(Constraint) = {
        import Constraint.*
        lit match {
            case StringLiteral(_)      => List(Just(prefix, LString()))
            case FloatLiteral(_)       => List(Just(prefix, LFloat()))
            case CharLiteral(_)        => List(Just(prefix, LChar()))
            case BoolLiteral(_)        => List(Just(prefix, LBool()))
            case WholeNumberLiteral(_) => ??? //TODO: Figure that out
            case DecimalLiteral(_)     => List(Just(prefix, LDecimal()))
        }
    }
    
}