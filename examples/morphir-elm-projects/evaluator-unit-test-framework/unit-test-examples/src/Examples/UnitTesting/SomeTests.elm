module Examples.UnitTesting.SomeTests exposing (..)
import Morphir.Testing.Test exposing (..)
import Examples.UnitTesting.SomeCode exposing (..)

fooTest : Test
fooTest = Assert ((foo 2) == 5)

fooTestFailure : Test
fooTestFailure = Assert ((foo 3) == 5)