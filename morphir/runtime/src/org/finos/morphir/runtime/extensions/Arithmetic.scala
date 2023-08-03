package org.finos.morphir.runtime.extensions

import org.finos.morphir.ir.FQName.FQName

object Arithmetic{
  trait ArithmeticOp extends {
    def apply[T : Numeric](a : T, b : T) : T
  }
  object ArithmeticOp {
    def apply[T : Numeric](f : (T, T) => T, name : FQName)
  }
}
