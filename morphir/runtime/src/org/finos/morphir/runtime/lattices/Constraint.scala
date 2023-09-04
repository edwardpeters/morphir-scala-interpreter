package org.finos.morphir.runtime.lattices

import org.finos.morphir.naming._
import org.finos.morphir.ir.{Type as T, Value as V}
import org.finos.morphir.ir.Value.{Value, Pattern, TypedValue, USpecification => UValueSpec}
import org.finos.morphir.ir.Type.{Type, UType, USpecification => UTypeSpec}

case class Constraint(lhs : LPName, function : LatticeFunction)
object Constraint{

    def function(function : LPName, arg : LPName, ret : LPName) : List[Constraint] = {
        List(
            Constraint(function, FunctionOf(arg, ret)),
            Constraint(arg, ArgOf(function)),
            Constraint(ret, RetOf(function))
        )
    }
    def evaluate(constraint, store : Map[LPName, LatticePoint]) : LatticePoint= {
        constraint match {
            case FunctionOf(_, arg, ret) => LatticePoint.LFunction(store(arg), store(ret))
            case ArgOf(_, function) =>
                //Function can be : Top, Funtion, Bottom, Other. 
                //Top => Top (if it's anything, it may be dioscovered to be a function later...)
                //Bottom => Bottom (also required, right... more specific input => at least as specific output, so if anything is bottom..)
                //Function => Function.arg, duh
                //Other => Uhh... Bottom?
                //What if function had tree parents/children? Well anything more specific needs arg and ret (right?)
                //What about wrappers? (Like Named?)
                //how about LCA(function, Function(Bottom, Bottom))?
        }
    }
}