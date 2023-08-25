module Morphir.SDKImpl.LocalDate exposing ()

{-

  val moduleSpec: Module.USpecification = Module.USpecification(
    types = Map(name("LocalDate") -> OpaqueTypeSpecification() ?? "Type that represents a date concept."),
    values = Map(
      vSpec("fromISO", "iso" -> stringType)(maybeType(localDateType)),
      vSpec("fromParts", "year" -> intType, "month" -> intType, "day" -> intType)(maybeType(localDateType)),
      vSpec("diffInDays", "date1" -> localDateType, "date2" -> localDateType)(intType),
      vSpec("diffInWeeks", "date1" -> localDateType, "date2" -> localDateType)(intType),
      vSpec("diffInMonths", "date1" -> localDateType, "date2" -> localDateType)(intType),
      vSpec("diffInYears", "date1" -> localDateType, "date2" -> localDateType)(intType),
      vSpec("addDays", "offset" -> intType, "startDate" -> localDateType)(localDateType),
      vSpec("addWeeks", "offset" -> intType, "startDate" -> localDateType)(localDateType),
      vSpec("addMonths", "offset" -> intType, "startDate" -> localDateType)(localDateType),
      vSpec("addYears", "offset" -> intType, "startDate" -> localDateType)(localDateType)
    )
  )
-}