module Morphir.Testing.Test exposing (..)

{-
    This module contains the Test type, which is treated specially by the evaluator for the "test" command.
    Unit tests should import this module, and each specific test or test suite should be a top-level value of type test.
 -}

{-
    Other test types:
    - Failure String : For a user-defined failure condition with a custom message
    - Success : For a user-defined success condition (i.e., if <someCondition> then (TestFailure "myMessage") else Success)
    - Description String Test: To create a named test
    - Suite (List Test) : For a suite of tests. (Note - maybe each should be required to have a name?)

 -}
type Test = 
    Assert Bool
