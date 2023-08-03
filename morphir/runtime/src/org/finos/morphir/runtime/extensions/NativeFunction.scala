package org.finos.morphir.runtime.extensions
object NativeFunction {
  sealed trait NativeFunction {
    val arity: Int
    val fqName: FQName
  }
  object NativeFunction{
    def apply[T, R](name : FQName, f : (T) => R): Unit ={
      new NativeFunction1{
        val fqName: FQName = name
        def apply(arg: T): R = f(arg)
      }
    }
    def apply[T1, T2, R](name: FQName, f: (T1, T2) => R): Unit = {
      new NativeFunction2 {
        val fqName: FQName = name
        def apply(arg1: T1, arg2 : T2): R = f(arg1, arg2)
      }
    }
    def apply[T1, T2, T3, R](name: FQName, f: (T1, T2, T3) => R): Unit = {
      new NativeFunction2 {
        val fqName: FQName = name
        def apply(arg1: T1, arg2: T2, arg3 : T3): R = f(arg1, arg2, arg3)
      }
    }
  }

  trait ArithmeticOp extends NativeFunction { //Do I want this to be a nativeFunction2? Kinda don't...
    val arity = 2
    def apply[T: Numeric](a: T, b: T): T
  }
  trait NativeFunction1[T, R] extends NativeFunction with ((T) => R) {
    final val arity = 1
    def apply(arg: T): R
  }
  trait NativeFunction2[T1, T2, R] extends NativeFunction with ((T1, T2) => R) {
    final val arity = 2
    def apply(arg1: T1, arg2: T2): R
  }
  trait NativeFunction3[T1, T2, T3, R] extends NativeFunction with ((T1, T2, T3) => R) {
    final val arity = 3
    def apply(arg1: T1, arg2: T2, arg3: T3): R
  }
  
}

/**
Problem cases:
number
comparable
iterable
eq
key?
parameterized:
  Can return Maybe, List, Maybe[List]
  Maybe taken by Result (only?)
Generic return => ?
