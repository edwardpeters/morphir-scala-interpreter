module Morphir.SDKImpl.Int exposing ()

{-

  val moduleSpec: Module.USpecification = Module.USpecification(
    types = Map(
      name("Int8")  -> OpaqueTypeSpecification() ?? "Type that represents a 8-bit integer.",
      name("Int16") -> OpaqueTypeSpecification() ?? "Type that represents a 16-bit integer.",
      name("Int32") -> OpaqueTypeSpecification() ?? "Type that represents a 32-bit integer.",
      name("Int64") -> OpaqueTypeSpecification() ?? "Type that represents a 64-bit integer."
    ),
    values = Map(
      vSpec("fromInt8", "n" -> int8Type)(intType),
      vSpec("toInt8", "n" -> intType)(maybeType(int8Type)),
      vSpec("fromInt16", "n" -> int16Type)(intType),
      vSpec("toInt16", "n" -> intType)(maybeType(int16Type)),
      vSpec("fromInt32", "n" -> int32Type)(intType),
      vSpec("toInt32", "n" -> intType)(maybeType(int32Type)),
      vSpec("fromInt64", "n" -> int64Type)(intType),
      vSpec("toInt64", "n" -> intType)(maybeType(int64Type))
    )
  )
-}