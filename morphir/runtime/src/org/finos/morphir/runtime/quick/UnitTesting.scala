package org.finos.morphir.runtime.quick


import org.finos.morphir.naming.*
import org.finos.morphir.ir.{Type as T, Value as V}
import org.finos.morphir.ir.Literal.Lit
import org.finos.morphir.ir.Value.{
  Pattern,
  TypedValue,
  Value,
  TypedDefinition as TypedValueDef,
  USpecification as UValueSpec
}
import org.finos.morphir.ir.Type.{Field, Type, UType, USpecification as UTypeSpec}
import org.finos.morphir.runtime.{RTValue, SDKConstructor, SDKValue, Utils, Extractors, Distributions, TestResult}
import Extractors.*

object UnitTesting {
  val testType = T.reference("Morphir.Testing", "Test", "Test")


  def runTest(
               globals: GlobalDefs,
               dists: Distributions,
               testName : FQName) : TestResult ={
    val refValue = Value.Reference.Typed(testType, testName)
    val loop = Loop(globals)
    val evaluated = loop.loop(refValue, Store.empty)
    evaluated match{
      case RTValue.ConstructorResult(FQString("Morphir.Testing:Test:assert"), List(RTValue.Primitive.Boolean(true))) => TestResult.Success(testName.toString)
      case RTValue.ConstructorResult(FQString("Morphir.Testing:Test:assert"), List(RTValue.Primitive.Boolean(false))) => TestResult.Failure(testName.toString, "Unrecognized assertion failure")
      case RTValue.ConstructorResult(otherName, _) => TestResult.Failure(testName.toString, s"Unrecognized constructor result: $otherName")
      case other => TestResult.Failure(testName.toString, s"Unrecognized value: ${other}")

    }
  }


  def runTests(
               globals: GlobalDefs,
               dists: Distributions,
               suiteName : String,
               testNames: FQName*): TestResult = {
    val results = testNames.toList.map(runTest(globals, dists, _))
    TestResult.Suite(suiteName, results)
  }

}
