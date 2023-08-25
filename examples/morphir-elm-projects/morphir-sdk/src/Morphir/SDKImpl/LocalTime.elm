module Morphir.SDKImpl.LocalTime exposing ()

{-

    types = Map(name("LocalTime") -> OpaqueTypeSpecification() ?? "Type that represents a time concept."),
    values = Map(
      vSpec("fromISO", "iso" -> stringType)(maybeType(localTimeType)),
      vSpec("fromMilliseconds", "millis" -> intType)(localTimeType),
      vSpec("diffInSeconds", "timeA" -> localTimeType, "timeB" -> localTimeType)(intType),
      vSpec("diffInMinutes", "timeA" -> localTimeType, "timeB" -> localTimeType)(intType),
      vSpec("diffInHours", "timeA" -> localTimeType, "timeB" -> localTimeType)(intType),
      vSpec("addSeconds", "seconds" -> intType, "time" -> localTimeType)(localTimeType),
      vSpec("addMinutes", "minutes" -> intType, "time" -> localTimeType)(localTimeType),
      vSpec("addHours", "hours" -> intType, "time" -> localTimeType)(localTimeType)
    )
  )
-}