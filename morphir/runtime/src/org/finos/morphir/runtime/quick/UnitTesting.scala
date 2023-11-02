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
import org.finos.morphir.runtime.{RTValue, SDKConstructor, SDKValue, Utils, Extractors, Distributions, TestResult, MorphirRuntimeError}
import org.finos.morphir.ir.printing.PrintIR
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
      case RTValue.ConstructorResult(FQString("Morphir.Testing:Test:assert"), List(RTValue.Primitive.Boolean(false))) => formatFailure(globals, dists, testName.toString, refValue)
      case RTValue.ConstructorResult(otherName, _) => TestResult.Failure(testName.toString, s"Unrecognized constructor result: $otherName")
      case other => TestResult.Failure(testName.toString, s"Unrecognized value: ${other}")

    }
  }


  private def formatFailure(globals : GlobalDefs, dists : Distributions, testName : String, ir : TypedValue) : TestResult.Failure= {
    ir match{
      case Value.Reference(_, name)                     => {
        val found = dists.lookupValueDefinition(name) match {
          case Left(value) => throw value.withContext("Definition not found while formatting error")
          case Right(value) => value
        }
        formatFailure(globals, dists, testName, found.body)
      }
      case Value.Apply(_, Value.Constructor(_, FQString("Morphir.Testing:Test:assert")), Value.Apply(_, Value.Apply(_, _, actual), expected)) => {
        val loop = Loop(globals)
        val actualEvaluated = loop.loop(actual, Store.empty)
        val expectedEvaluated = loop.loop(expected, Store.empty)
        TestResult.Failure(testName,
          s"""
             |\t\t($actual != $expected)
             |\t\t${PrintIR(actualEvaluated)} != ${PrintIR(expectedEvaluated)}""".stripMargin)
      }
      case other => TestResult.Failure(testName, s"($ir) evaluated to false")
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
