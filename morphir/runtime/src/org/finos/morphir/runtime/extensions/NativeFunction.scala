package org.finos.morphir.runtime.extensions
object NativeFunction {
  sealed trait NativeFunction {
    def fqName: FQName
  }
  object NativeFunction{
    def apply[T, R](name : FQName, f : (T) => R): Unit ={
      new NativeFunction1{
        def fqName: FQName = name
        def apply(arg: T): R = f(arg)
      }
    }
  }

  trait NativeFunction1[T, R] extends NativeFunction with ((T) => R) {
    def apply(arg: T): R
  }
  trait NativeFunction2[T1, T2, R] extends NativeFunction with ((T1, T2) => R) {
    def apply(arg1: T1, arg2: T2): R
  }

  trait NativeFunction3[T1, T2, T3, R] extends NativeFunction with ((T1, T2, T3) => R) {
    def apply(arg1: T1, arg2: T2, arg3: T3): R
  }
  
}
