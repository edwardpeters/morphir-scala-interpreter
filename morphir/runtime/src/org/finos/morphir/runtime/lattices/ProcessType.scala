package org.finos.morphir.runtime.lattices

import org.finos.morphir.naming._
import org.finos.morphir.ir.{Type as T, Value as V}
import org.finos.morphir.ir.Value.{Value, Pattern, RawValue, USpecification => UValueSpec}
import org.finos.morphir.ir.Type.{Type, UType, USpecification => UTypeSpec}
import org.finos.morphir.ir.Literal.Lit

object ProcessType{
    def uType(prefix : LPName, tpe : UType) => List[Constraint]{
        tpe match {
            /**
             * TODO: Review WTF these even are now
             */
            case Type.ExtensibleRecord((), name, fields) => ??? //Min type, but need variable handling...
            case Type.Record((), fields) => ??? //Max and min type. (Exact type should be suffic?)
            case Type.Variable((), name) => LTVar(name, ???) //TVAR_SCOPE : Here it is just from the type name...
                //So here we need to say that Prefix == LTVar in this scope... 
            case Type.Reference((), typeName, typeArgs) => ??? //The eternal question... dealias or nah?
                typeArgs.toList.zipWithIndex.flatMap{case (arg, index) => {
                    val argName = s"$prefix:$typeName.args[$index]"
                    List(
                        Constraint(argName, ReferenceArgAt(prefix, index)),
                        Constraint(prefix, RefernceArgOf(argName, index)),
                        utype(argName, arg)
                    ) 
                    
                } ++ List(
                    Constraint(prefix, Just(LRef(typeArgs.map(_ => Top)))) //easier to give it the shape as a constraint
                )
            }
            /**
             * Sooo I think we just haver a "Reference" constraint
             */
            case Type.Function((), arg, ret) => {
                val argName = prefix.append(".arg")
                val retName = prefix.append(".ret")
                Constraint.function(prefix, argName, retName) ++
                uType(argName, arg) ++
                uType(retName, ret)
            }
            case Type.Tuple((), elements) => {
                elements.toList.zipWithIndex.flatMap{case (elem, index) => {
                    val elemName = s"$prefix._$index"
                    List(
                        Constraint(elemName, TupleAt(prefix, index)),
                        Constraint(prefix, TupleOf(elemName, index)),
                        uType(elemName, elem)
                    )
                } ++ List(
                    Constraint(prefix, Just(LTuple(elements.map(_ => Top)))) //easier to give it the shape as a constraint
                )
            }
            }
            case Type.Unit(())           => List(Constraint(prefix, Just(LUnit)))
            case IntRef()       => ???
            case MaybeRef(elementType) => ???
            case DictRef(keyType, valType) => ???
            case SetRef(elementType) => ???
            case Int16Ref()     => ???
            case Int32Ref()     => ???
            case Int64Ref()     => ???
            case StringRef()    => ???
            case BoolRef()      => ???
            case CharRef()      => ???
            case FloatRef()     => ???
            case DecimalRef()   => ???
            case LocalDateRef() => ???
            case LocalTimeRef() => ???
        }
    }
}