package org.finos.morphir.runtime.lattices

import org.finos.morphir.naming._
import org.finos.morphir.ir.{Type as T, Value as V}
import org.finos.morphir.ir.Value.{Value, Pattern, RawValue, USpecification => UValueSpec}
import org.finos.morphir.ir.Type.{Type, UType, USpecification => UTypeSpec}

type Part
case class LPName(){
    def append(part : Part) : LPName = ???
}