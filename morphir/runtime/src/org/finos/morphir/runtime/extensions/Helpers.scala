object Helpers {

  trait NativeFunction2[T1, T2, R] extends NativeFunction with ((T1, T2) => R) {
    def apply(arg1: T1, arg2: T2): R
  }

  trait NativeFunction3[T1, T2, T3, R] extends NativeFunction with ((T1, T2, T3) => R) {
    def apply(arg1: T1, arg2: T2, arg3: T3): R
  }
}
