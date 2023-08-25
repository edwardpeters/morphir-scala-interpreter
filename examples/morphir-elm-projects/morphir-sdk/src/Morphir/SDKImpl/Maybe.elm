module Morphir.SDKImpl.Maybe exposing ()

{-

  val moduleSpec: Module.USpecification = Module.USpecification(
    types = Map(
      name("Maybe") -> CustomTypeSpecification(
        Chunk(name("a")),
        UConstructors(
          Map(name("Just") -> Chunk((name("value"), variable(name("a")))), name("Nothing") -> Chunk.empty)
        )
      ) ?? "Type that represents an optional value."
    ),
    values = Map(
      vSpec("andThen", "f" -> tFun(tVar("a"))(maybeType(tVar("b"))), "maybe" -> maybeType(tVar("a")))(
        maybeType(tVar("b"))
      ),
      vSpec("map", "f" -> tFun(tVar("a"))(tVar("b")), "maybe" -> maybeType(tVar("a")))(maybeType(tVar("b"))),
      vSpec(
        "map2",
        "f"      -> tFun(tVar("a"), tVar("b"))(tVar("r")),
        "maybe1" -> maybeType(tVar("a")),
        "maybe2" -> maybeType(tVar("b"))
      )(maybeType(tVar("r"))),
      vSpec(
        "map3",
        "f"      -> tFun(tVar("a"), tVar("b"), tVar("c"))(tVar("r")),
        "maybe1" -> maybeType(tVar("a")),
        "maybe2" -> maybeType(tVar("b")),
        "maybe3" -> maybeType(tVar("c"))
      )(maybeType(tVar("r"))),
      vSpec(
        "map4",
        "f"      -> tFun(tVar("a"), tVar("b"), tVar("c"), tVar("d"))(tVar("r")),
        "maybe1" -> maybeType(tVar("a")),
        "maybe2" -> maybeType(tVar("b")),
        "maybe3" -> maybeType(tVar("c")),
        "maybe4" -> maybeType(tVar("d"))
      )(maybeType(tVar("r"))),
      vSpec(
        "map5",
        "f"      -> tFun(tVar("a"), tVar("b"), tVar("c"), tVar("d"), tVar("e"))(tVar("r")),
        "maybe1" -> maybeType(tVar("a")),
        "maybe2" -> maybeType(tVar("b")),
        "maybe3" -> maybeType(tVar("c")),
        "maybe4" -> maybeType(tVar("d")),
        "maybe5" -> maybeType(tVar("e"))
      )(maybeType(tVar("r"))),
      vSpec("withDefault", "default" -> tVar("a"), "maybe" -> maybeType(tVar("a")))(tVar("a"))
    )
  )
-}