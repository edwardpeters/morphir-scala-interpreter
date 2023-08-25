module Morphir.SDKImpl.Number exposing ()

{-


  val moduleSpec: Module.USpecification = Module.USpecification(
    types = Map(name("Decimal") -> OpaqueTypeSpecification() ?? "Type that represents a Decimal."),
    values = Map(
      vSpec("fromInt", "n" -> intType)(numberType),
      vSpec("equal", "a" -> numberType, "b" -> numberType)(boolType),
      vSpec("notEqual", "a" -> numberType, "b" -> numberType)(boolType),
      vSpec("lessThan", "a" -> numberType, "b" -> numberType)(boolType),
      vSpec("lessThanOrEqual", "a" -> numberType, "b" -> numberType)(boolType),
      vSpec("greaterThan", "a" -> numberType, "b" -> numberType)(boolType),
      vSpec("greaterThanOrEqual", "a" -> numberType, "b" -> numberType)(boolType),
      vSpec("add", "a" -> numberType, "b" -> numberType)(numberType),
      vSpec("subtract", "a" -> numberType, "b" -> numberType)(numberType),
      vSpec("multiply", "a" -> numberType, "b" -> numberType)(numberType),
      vSpec("divide", "a" -> numberType, "b" -> numberType)(resultType(divisionByZeroType, numberType)),
      vSpec("abs", "value" -> numberType)(numberType),
      vSpec("negate", "value" -> numberType)(numberType),
      vSpec("reciprocal", "value" -> numberType)(numberType),
      vSpec("coerceToDecimal", "default" -> decimalType, "number" -> numberType)(decimalType),
      vSpec("toDecimal", "number" -> numberType)(maybeType(decimalType)),
      vSpec("toFractionalString", "num" -> numberType)(stringType),
      vSpec("simplify", "value" -> numberType)(maybeType(numberType)),
      vSpec("isSimplified", "a" -> numberType)(boolType),
      vSpec("zero")(numberType),
      vSpec("one")(numberType)
    )
  )
-}