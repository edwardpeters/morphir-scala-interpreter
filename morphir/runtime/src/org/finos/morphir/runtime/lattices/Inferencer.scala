package org.finos.morphir.runtime.lattices

import org.finos.morphir.naming._
import org.finos.morphir.ir.{Type as T, Value as V}
import org.finos.morphir.ir.Value.{Value, Pattern, TypedValue, USpecification => UValueSpec}
import org.finos.morphir.ir.Type.{Type, UType, USpecification => UTypeSpec}
import org.finos.morphir.runtime.Distributions

object Inferencer{
    def infer(dists : Distributions) {
        //Collect everything (with names)
        val types = ???
        val defs = ???
        val typeConstraints = ???
            //Aliases: Dealias later? Interesting... That's fine iff we check arity.
            //Constructors: Constructor branches will need to be referenced during constraint gen, but do not go to their own LPoints
        val defConstraints = ???//Process defs to... values, with TVars in scope?
            //Collect TVars, give scope by FQName of top-level def
            //ProcessType of each arg node
            //ProcessType of ret node
            //ProcessValue of body
        val reduced = ???
            //group constraitns by LHS
            //for all with more than one:
                //rename existing to name1, name2, etc
                //add constraint name, GCD(name2, name3, ...)
        val simplified = ???
            //Pre-check for equality clusters, bind to single name (that's fine)
            //This is for performance
        val augmented = ???
            //For all LP, add the names to the lattice point... these will 
        var bindings : Map[LPName, LatticePoint] = ??? //Initialize with every LPName bound to top
        var changes = true
        while (changes){
            changes = false
            simplified.forall(constraint => {
                val newValue = Constraint.evaluate(constraint, bindings)
                if newValue == Bottom() {
                    //Error!
                }
                else if newValue != bindings(constraint.left){
                    bindings(constraint.left = newValue)
                    changes = true
                }
            })
        }
        //???
        //Profit
        val inferredTypes = ???
        //Group names of bindings, make them generics
        //Okay, but what about ordering/circular stuff?
        /**
         * let rec a = b, b = a
         *
         * This yields four points, correct?
         * aDef, aRef, bDef, bRef
         * aDef = bRef <- aDef... um. 
         * Okay let's try again
         * let rec a = ..., b = a
         * //so now a is T, b is T {a, b} - {a, b} may be recognized as a group w/ a dependency
         * //Can you ever not order those?

        
        
    }
}