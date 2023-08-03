object Helpers {
  sealed trait NativeFunction {
    def fqName: FQName
  }

  trait NativeFunction1[T, R] extends NativeFunction with ((T) => R){
    def apply(arg : T) : R
  }
}
