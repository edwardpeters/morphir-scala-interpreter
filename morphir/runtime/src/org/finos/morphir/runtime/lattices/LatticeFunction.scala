package org.finos.morphir.runtime.lattices

import org.finos.morphir.naming._
import org.finos.morphir.ir.{Type as T, Value as V}
import org.finos.morphir.ir.Value.{Value, Pattern, TypedValue, USpecification => UValueSpec}
import org.finos.morphir.ir.Type.{Type, UType, USpecification => UTypeSpec}

sealed trait LatticeFunction{

}
object LatticeFunction{
    /**
     * For constraints with no dependencies - i.e., the RHS is fully resolved (Possibly to TVar)
     */ 
    case class Just(lp : LatticePoint) extends LatticeFunction
    case class FunctionOf(arg : LPName, ret : LPName) extends LatticeFunction
    case class ArgOf(function : LPName) extends LatticeFunction
    case class RetOf(function : LPName) extends LatticeFunction
    case class TupleOf(nested : LPName, index : Int) extends LatticeFunction
    case class TupleAt(nested : LPName, index : Int) extends LatticeFunction
    case class RefernceArgOf(nested : LPName, index : Int) extends LatticeFunction
    case class ReferenceArgAt(nested : LPName, index : Int) extends LatticeFunction
}