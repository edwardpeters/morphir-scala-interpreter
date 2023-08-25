module Morphir.SDKImpl.Rule exposing ()

{-


  val moduleSpec: Module.USpecification = Module.USpecification(
    types = Map(
      name("Rule") -> TypeAliasSpecification(
        Chunk(name("a"), name("b")),
        tFun(tVar("a"))(maybeType(tVar("b")))
      ) ?? "Type that represents an rule."
    ),
    values = Map(
      vSpec("chain", "rules" -> listType(ruleType(tVar("a"), tVar("b"))))(ruleType(tVar("a"), tVar("b"))),
      vSpec("any", "value" -> tVar("a"))(boolType),
      vSpec("is", "ref" -> tVar("a"), "value" -> tVar("a"))(boolType),
      vSpec("anyOf", "ref" -> listType(tVar("a")), "value" -> tVar("a"))(boolType),
      vSpec("noneOf", "ref" -> listType(tVar("a")), "value" -> tVar("a"))(boolType)
    )
  )
-}