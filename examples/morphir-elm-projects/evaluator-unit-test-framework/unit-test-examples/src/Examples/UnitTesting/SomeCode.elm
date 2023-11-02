module Examples.UnitTesting.SomeCode exposing (..)

timesTwoPlusOne : Int -> Int
timesTwoPlusOne x = (x * 2) + 1

tupleReverse : (a, b) -> (b, a)
tupleReverse (a, b) = (a, a)

isPrime : Int -> Bool
isPrime x = case x of
    3 -> True
    _ -> False