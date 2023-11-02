module Examples.UnitTesting.SomeTests exposing (..)
import Morphir.Testing.Test exposing (..)
import Examples.UnitTesting.SomeCode exposing (..)

timesTwoPlusOneTest : Test
timesTwoPlusOneTest = Assert ((timesTwoPlusOne 2) == 5)

timesTwoPlusOneTestFailure : Test
timesTwoPlusOneTestFailure = Assert ((timesTwoPlusOne 3) == 5)

tupleReverseTest : Test
tupleReverseTest = Assert ((tupleReverse (1,2)) == (2, 1))

primeTest : Test
primeTest = Assert (isPrime 5)