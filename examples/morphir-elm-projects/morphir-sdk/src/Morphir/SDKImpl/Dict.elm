module Morphir.SDKImpl.Dict exposing ()
import Morphir.SDKImpl.NativeTypes exposing (NativeDict)
--import Dict as NativeDict

{-@NATIVE_TYPE-}
type alias Dict comparble v = NativeDict comparable v

{-@NATIVE_DEF-}
fromList : List (comparble, v) -> Dict comparable v
fromList l = fromList l

empty : Dict comparable v
empty = fromList []

singleton : comparable -> v -> Dict comparable val
singleton key value = fromList[(key, value)]

{-

  val moduleSpec: Module.USpecification = Module.USpecification(
    types = Map(
      name("Dict") -> OpaqueTypeSpecification("k", "v") ?? "Type that represents a dictionary of key-value pairs."
    ),
    values = Map(
      vSpec("empty")(dictType(tVar("k"), tVar("v"))),
      vSpec("singleton", "key" -> tVar("comparable"), "value" -> tVar("v"))(dictType(tVar("comparable"), tVar("v"))),
      vSpec(
        "insert",
        "key"   -> tVar("comparable"),
        "value" -> tVar("v"),
        "dict"  -> dictType(tVar("comparable"), tVar("v"))
      )(dictType(tVar("comparable"), (tVar("v")))),
      vSpec(
        "update",
        "key"  -> tVar("comparable"),
        "f"    -> tFun(maybeType(tVar("v")))(maybeType(tVar("v"))),
        "dict" -> dictType(tVar("comparable"), tVar("v"))
      )(dictType(tVar("comparable"), tVar("v"))),
      vSpec("remove", "key" -> tVar("comparable"), "dict" -> dictType(tVar("comparable"), tVar("v")))(
        dictType(tVar("comparable"), tVar("v"))
      ),
      vSpec("isEmpty", "dict" -> dictType(tVar("comparable"), tVar("v")))(boolType),
      vSpec("member", "key" -> tVar("comparable"), "dict" -> dictType(tVar("comparable"), tVar("v")))(boolType),
      vSpec("get", "key" -> tVar("comparable"), "dict" -> dictType(tVar("comparable"), tVar("v")))(
        maybeType(tVar("v"))
      ),
      vSpec("size", "dict" -> dictType(tVar("comparable"), tVar("v")))(intType),
      vSpec("keys", "dict" -> dictType(tVar("k"), tVar("v")))(listType(tVar("k"))),
      vSpec("values", "dict" -> dictType(tVar("k"), tVar("v")))(listType(tVar("v"))),
      vSpec("toList", "dict" -> dictType(tVar("k"), tVar("v")))(listType(tuple(Chunk(tVar("k"), tVar("v"))))),
      vSpec("fromList", "list" -> listType(tuple(Chunk(tVar("comparable"), tVar("v")))))(
        dictType(tVar("comparable"), tVar("v"))
      ),
      vSpec("map", "f" -> tFun(tVar("k"), tVar("a"))(tVar("b")), "dict" -> dictType(tVar("k"), tVar("a")))(
        dictType(tVar("k"), tVar("b"))
      ),
      vSpec(
        "foldl",
        "f"    -> tFun(tVar("k"), tVar("v"), tVar("b"))(tVar("b")),
        "z"    -> tVar("b"),
        "list" -> dictType(tVar("k"), tVar("v"))
      )(tVar("b")),
      vSpec(
        "foldr",
        "f"    -> tFun(tVar("k"), tVar("v"), tVar("b"))(tVar("b")),
        "z"    -> tVar("b"),
        "list" -> dictType(tVar("k"), tVar("v"))
      )(tVar("b")),
      vSpec(
        "filter",
        "f"    -> tFun(tVar("comparable"), tVar("v"))(boolType),
        "dict" -> dictType(tVar("comparable"), tVar("v"))
      )(dictType(tVar("comparable"), tVar("v"))),
      vSpec(
        "partition",
        "f"    -> tFun(tVar("comparable"), tVar("v"))(boolType),
        "dict" -> dictType(tVar("comparable"), tVar("v"))
      )(tuple(Chunk(dictType(tVar("comparable"), tVar("v")), dictType(tVar("comparable"), tVar("v"))))),
      vSpec(
        "union",
        "dict1" -> dictType(tVar("comparable"), tVar("v")),
        "dict2" -> dictType(tVar("comparable"), tVar("v"))
      )(dictType(tVar("comparable"), tVar("v"))),
      vSpec(
        "intersect",
        "dict1" -> dictType(tVar("comparable"), tVar("v")),
        "dict2" -> dictType(tVar("comparable"), tVar("v"))
      )(dictType(tVar("comparable"), tVar("v"))),
      vSpec(
        "diff",
        "dict1" -> dictType(tVar("comparable"), tVar("v")),
        "dict2" -> dictType(tVar("comparable"), tVar("v"))
      )(dictType(tVar("comparable"), tVar("v"))),
      vSpec(
        "merge",
        "leftOnly"  -> tFun(tVar("comparable"), tVar("a"), tVar("result"))(tVar("result")),
        "both"      -> tFun(tVar("comparable"), tVar("a"), tVar("b"), tVar("result"))(tVar("result")),
        "rightOnly" -> tFun(tVar("comparable"), tVar("b"), tVar("result"))(tVar("result")),
        "dictLeft"  -> dictType(tVar("comparable"), tVar("a")),
        "dictRight" -> dictType(tVar("comparable"), tVar("b")),
        "input"     -> tVar("result")
      )(tVar("result"))
    )
  )
-}