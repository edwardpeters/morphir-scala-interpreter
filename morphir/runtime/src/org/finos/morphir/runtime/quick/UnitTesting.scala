package org.finos.morphir.runtime.quick

import org.finos.morphir.datamodel.{Concept, Data, EnumLabel, Label}
import org.finos.morphir.runtime.{Distributions, RTValue}
import org.finos.morphir.ir.{Type => T, Value => V}
import org.finos.morphir.runtime.exports.*
import org.finos.morphir.runtime.environment.MorphirEnv
import org.finos.morphir.runtime.services.sdk.MorphirSdk
import org.finos.morphir.runtime.TestSummary
import org.finos.morphir.runtime.SDKValue.SDKValueDefinition
import org.finos.morphir.ir.Value.*
import org.finos.morphir.ir.Value.Pattern
import org.finos.morphir.naming.*
import org.finos.morphir.runtime.MorphirRuntimeError.*
import org.finos.morphir.runtime.internal.NativeFunctionAdapter
import org.finos.morphir.ir.sdk
import org.finos.morphir.runtime.SDKValue
import org.finos.morphir.runtime.TestTree
import org.finos.morphir.runtime.MorphirUnitTest
import org.finos.morphir.runtime.SingleResult
import org.finos.morphir.runtime.RTValue.Primitive
import org.finos.morphir.runtime.RTValue as RT
import org.finos.morphir.util.PrintRTValue
import org.finos.morphir.runtime.Extractors.*

import org.finos.morphir.runtime.internal._

object UnitTestingSDK {
  def expectation(result: RTValue) =
    RTValue.ConstructorResult(FQName.fromString("Morphir.UnitTest:Expect:Expectation"), List(result))
  val passed = RTValue.ConstructorResult(FQName.fromString("Morphir.UnitTest:Expect:Pass"), List())
  def failed(msg: String) =
    RTValue.ConstructorResult(FQName.fromString("Morphir.UnitTest:Expect:Fail"), List(Primitive.String(msg)))

  val equal: SDKValue =
    SDKValue.SDKNativeFunction.fun2 { (a: RTValue, b: RTValue) =>
      val result = if (a == b) passed else failed(s"${PrintRTValue(a).plainText} != ${PrintRTValue(b).plainText}")
      expectation(result)
    }

  val equalIntrospected: SDKValue =
    SDKValue.SDKNativeFunction.fun1 { (a: RTValue) =>
      val result = throw OtherError("At least we got this far: ", a)
      result
    }

  val equalIntrospectedV2 = DynamicNativeFunction1("equalIntrospected") {
    (ctx: NativeContext) => (f: RTValue.Function) =>
      {
        val irString = PrintRTValue(f).plainText
        val out      = ctx.evaluator.handleApplyResult(T.unit, f, RTValue.Unit())
        val (rt1, rt2) = out match {
          case RT.Tuple(List(rt1_, rt2_)) => (rt1_, rt2_)
          case other                      => throw new Exception("This should not be!")
        }
        val res = if (rt1 != rt2) failed(s"$irString worked out with ${PrintRTValue(rt1)} != ${PrintRTValue(rt2)}")
        else passed
        expectation(res)
      }
  }

  val newDefs = GlobalDefs(
    Map(FQName.fromString("Morphir.UnitTest:Expect:equalIntrospected") -> NativeFunctionAdapter.Fun1(
      equalIntrospectedV2
    ).realize),
    Map()
  )

}

object UnitTesting {

  // What should this method's types be?
  // Could return test failures on the error channel and have a unit return type
  // Or could return a test report, with no error (barring evaluation failures?)
  // Need to return a summary even on success, so not that first one
  // Seems messy to return a summary either way
  // Also, I would say "Testing" didn't fail even if the tests did
  def testType        = T.reference("Morphir.UnitTest", "Test", "Test")
  def testResultType  = T.reference("Morphir.UnitTest", "Test", "TestResult")
  def expectationType = T.reference("Morphir.UnitTest", "Expect", "Expectation")
  private[runtime] def runTests(
      globals: GlobalDefs,
      dists: Distributions
  ): RTAction[MorphirEnv, Nothing, TestSummary] =
    RTAction.environmentWithPure[MorphirSdk] { env =>
      val testNames = collectTests(globals, dists)
      val testVals  = testNames.map(fqn => Value.Reference.Typed(testType, fqn))
      if (testVals.isEmpty) {
        val emptySummary = TestSummary("No tests run", true)
        RTAction.succeed(emptySummary)
      } else {
        val testSuiteIR = if (testVals.length == 1)
          testVals.head
        else {
          val testList = V.list(sdk.List.listType(testType), testVals: _*)
          V.applyInferType(
            testType,
            V.constructor(FQName.fromString("Morphir.UnitTest:Test:Concat")),
            testList
          )
        }

        val runTestsIR = V.applyInferType(
          testResultType,
          V.reference(FQName.fromString("Morphir.UnitTest:Test:run")),
          testSuiteIR
        )
        val testsPassedIR = V.applyInferType(
          sdk.Basics.boolType,
          V.reference(FQName.fromString("Morphir.UnitTest:Test:passed")),
          runTestsIR
        )

        // We evaluate twice (in failed cases) intentionally - once in a pure-elm manner to ensure we are using
        // the in-flight evaluator, then again with special code enabled to better analyze and report the failures.
        // The first assures us we have no "false nagative" (i.e., falsely passing) tests
        // The second lets us give the user lots of information about why and how tests failed
        val passedResult =
          try
            Right(EvaluatorQuick.eval(testsPassedIR, globals, dists))
          catch {
            case e => Left(e)
          }

        val report = passedResult match {
          case Right(Data.Boolean(true)) => passingResult(globals, dists, runTestsIR)
          // Anything else and we know we have a failure, it's just a matter of determining what
          case _ => nonPassingResult(globals, dists, testNames)
        }

        // val newGlobals =
        //   globals.withDefinition(FQName.fromString("Morphir.UnitTest:Expect:equal"), UnitTestingSDK.equal)
        RTAction.succeed(report)
      }
    }

  private[runtime] def passingResult(
      globals: GlobalDefs,
      dists: Distributions,
      runTestsIR: TypedValue
  ): TestSummary = {
    val reportIR = V.applyInferType(
      sdk.String.stringType,
      V.reference(FQName.fromString("Morphir.UnitTest:Test:resultToString")),
      runTestsIR
    )
    val mdmReport = EvaluatorQuick.eval(reportIR, globals, dists)
    val report = mdmReport match {
      case Data.String(s) => s
      case _              => throw new OtherError("Test result: ", mdmReport)
    }
    TestSummary(report, true)
  }

  private[runtime] def nonPassingResult(
      globals: GlobalDefs,
      dists: Distributions,
      testNames: List[FQName]
  ): TestSummary = {
    // Let's just eat the whole horse
    // val testSuiteRT = Loop(globals).loop(testSuiteIR, Store.empty)
    val testIRs: List[(FQName, TypedValue)] =
      testNames.map(fqn => (fqn, Value.Reference.Typed(testType, fqn)))

    // We need to evaluate to resolve user code, but we want the values of the actual calls to Expect functions
    // So we replace such calls with thunks; after evaluation, the IR will be intact for inspection
    // def delay(value : TypedValue) : TypedValue = {
    //   Lambda(Pattern.UnitPattern, value)
    // }
    def thunkify(value: TypedValue): Option[TypedValue] = {
      import org.finos.morphir.ir.Value.Value.{List as ListValue, *}
      value match {
        case Apply(_, Apply(_, Reference(_, FQString("Morphir.UnitTest:Expect:equal")), arg1IR), arg2IR) =>
          val res = Some(
            V.applyInferType(
              expectationType,
              V.reference(FQName.fromString("Morphir.UnitTest:Expect:equalIntrospected")),
              V.lambda(
                T.function(T.unit, T.tuple(List(arg1IR.attributes, arg2IR.attributes))),
                Pattern.UnitPattern(T.unit),
                V.tuple(T.tuple(List(arg1IR.attributes, arg2IR.attributes)), arg1IR, arg2IR)
              )
            )
          )
          res
        // throw OtherError("Match hppened, something else did not")
        // case Apply(_, Apply(_, fqn, _), _) => throw OtherError("Unexpected double apply", fqn)
        // case other                         => throw OtherError("Literally runnings,", other)
        case _ => None
      }
    }
    def thunkifyTransform = transform(thunkify(_))
    // val thunkifiedTests   = testIRs.map { case (fqn, value) => (fqn -> thunkifyTransform(value)) }

    val newGlobalDefs = globals.definitions.map {
      case (fqn, SDKValue.SDKValueDefinition(dfn)) =>
        (fqn, SDKValue.SDKValueDefinition(dfn.copy(body = thunkifyTransform(dfn.body))))
      case other => other
    }
    val newGlobals = globals.copy(definitions = newGlobalDefs).withBindingsFrom(UnitTestingSDK.newDefs)
    // Wait we want to RUN the expect function, but w/ a superprivileged SDK function replacing the test function
    // So that means that any call that looks like
    // (Apply(F, Arg) : Expect) //No wait this includes the wrong stuffs
    // Okay if the users are that determined to break test reporting they can, it won't change passes to fails
    // So that's
    // Match once and in this order:
    // Apply(Apply(Ref(OneOfThem), Arg1), Arg2) -> Apply(Ref(OneOfThem), () -> (Arg1, Arg2)) //Okay?
    // Apply(Reference(OneOfThem), Arg) -> Apply(Reference(OneOfThem), () -> Arg)
    // Tho TBH, which even need this?
    // Let's just say all of them. It's nice to be able to SEE the IR.

    // Transformation is hard tho - revisit that idea with greater patience.

    // Then There is...
    // Apply(Apply(Ref(OnFail)), Apply(...))
    // So I think OnFail we DO replace but we DO NOT convert, right? That sounds good.

    val testRTValues: List[(FQName, Either[Throwable, RTValue])] = testIRs
      .map { case (fqn, ir) =>
        try
          (fqn, Right(Loop(newGlobals).loop(ir, Store.empty)))
        catch {
          case e => (fqn, Left(e))
        }
      }

    // Try to convert these to actual Test trees
    val testTree: MorphirUnitTest =
      TestTree.Concat(
        testRTValues.map {
          case (fqn, Left(err)) => TestTree.Error(fqn.toString, err)
          case (fqn, Right(rt)) =>
            TestTree.fromRTValue(rt) match {
              case d: TestTree.Describe[_]   => d
              case s: TestTree.SingleTest[_] => s
              case other                     => TestTree.Describe(fqn.toString, List(other))
            }
        }
      ).resolveOnly

    def getExpects(test: MorphirUnitTest): MorphirUnitTest =
      import TestTree.*
      test match {
        case Describe(desc, tests) => Describe(desc, tests.map(getExpects))
        case Concat(tests)         => Concat(tests.map(getExpects))
        case Only(inner)           => Only(getExpects(inner))
        case SingleTest(desc, thunk) => SingleTest(
            desc,
            Loop(newGlobals)
              .handleApplyResult(
                testType,
                thunk,
                RTValue.Unit()
              )
          )
        case other => other // err, todo, skip lack anything to resolve
      }

    val withExpects = getExpects(testTree)

    def formatExpects(tree: MorphirUnitTest): TestTree[SingleResult] = {
      import TestTree.*
      import SingleResult.*
      tree match {
        case Describe(desc, tests) => Describe(desc, tests.map(formatExpects))
        case Concat(tests)         => Concat(tests.map(formatExpects))
        case Only(inner)           => Only(formatExpects(inner))
        case SingleTest(
              desc,
              RT.ConstructorResult(FQStringTitleCase("Morphir.UnitTest:Expect:Expectation"), List(rt))
            ) =>
          rt match {
            case RT.ConstructorResult(FQStringTitleCase("Morphir.UnitTest:Expect:Pass"), List()) =>
              SingleTest(desc, Passed())
            case RT.ConstructorResult(
                  FQStringTitleCase("Morphir.UnitTest:Expect:Fail"),
                  List(Primitive.String(msg))
                ) =>
              SingleTest(desc, Failed(msg))
            case other => throw new OtherError("Unexpected Expectation", other)
          }
        case SingleTest(desc, other) => throw new OtherError("Test $desc had unexpected result structure", other)
        case other: Error            => other
        case other: Skip             => other
        case other: Todo             => other
      }
    }

    val treeWithResults = formatExpects(withExpects)

    throw new OtherError("Ned got tired of coding", TestTree.toReport(treeWithResults))

    // Each test leaf contains a thunk
    // We evaluate the thunks, we get back:
    // Expects or
    // More Thunks (Re-evaluate once)
    // () -> Expect.Something(args)
    // () -> (() -> Expect.somethig(args)) //WAIT WRONG THAT'S A MIX OF TYPE AND VALUE
    // Deconvert first? Or what?
    // Umm... wait did we think this thru?
    // We have a number of
    // Replace all Apply(Apply(Expect.whatever)) w/ Lambda (() -> That Nonsense)
    // Evaluate again; failures caught as errors, not thrown
    //
  }

  private[runtime] def collectTests(
      globals: GlobalDefs,
      dists: Distributions
  ): List[FQName] = {
    val tests = globals.definitions.collect { // TODO: Improved test recognition (aliasing of return type, cleanup, ???)
      case (fqn -> SDKValueDefinition(definition: TypedDefinition))
          if (definition.inputTypes.isEmpty && definition.outputType == testType) =>
        fqn
    }.toList
    tests
  }

  private[runtime] def transform(partial: TypedValue => Option[TypedValue])(value: TypedValue): TypedValue = {
    import org.finos.morphir.ir.Value.Value.{List as ListValue, *}
    def recurse = transform(partial)
    partial(value) match {
      case Some(newValue) => newValue
      case None => value match {
          case Apply(va, function, argument) => Apply(va, recurse(function), recurse(argument))
          case Destructure(va, pattern, valueToDestruct, inValue) =>
            Destructure(va, pattern, recurse(valueToDestruct), recurse(inValue))
          case Field(va, recordValue, name) => Field(va, recurse(recordValue), name)
          case IfThenElse(va, condition, thenValue, elseValue) =>
            IfThenElse(va, recurse(condition), recurse(thenValue), recurse(elseValue))
          case Lambda(va, pattern, body) => Lambda(va, pattern, recurse(body))
          case LetDefinition(va, name, definition, inValue) =>
            LetDefinition(va, name, definition.copy(body = recurse(definition.body)), recurse(inValue))
          case LetRecursion(va, definitions, inValue) => LetRecursion(
              va,
              definitions.map((name, dfn) => (name, dfn.copy(body = recurse(dfn.body)))),
              recurse(inValue)
            )
          case ListValue(va, elements) => ListValue(va, elements.map(recurse))
          case PatternMatch(va, value, cases) =>
            PatternMatch(va, recurse(value), cases.map((casePattern, caseValue) => (casePattern, recurse(caseValue))))
          case Record(va, fields) => Record(
              va,
              fields.map((fieldName, fieldValue) => (fieldName, recurse(fieldValue)))
            )
          case Tuple(va, elements) => Tuple(
              va,
              elements.map(recurse)
            )
          case UpdateRecord(va, valueToUpdate, fields) => UpdateRecord(
              va,
              recurse(valueToUpdate),
              fields.map((fieldName, fieldValue) => (fieldName, recurse(fieldValue)))
            )
          case noNestedIR => noNestedIR
        }
    }
  }

}

//COPIED:

// package org.finos.morphir.runtime.quick

// private[runtime] def runTests(
//       globals: GlobalDefs,
//       dists: Distributions
//   ): TestResult = {
//     val testType = T.reference("Morphir.Testing", "Test", "Test")
//     val tests = globals.definitions.collect { // TODO: Improved test recognition (aliasing of return type, cleanup, ???)
//       case (fqn -> SDKValue.SDKValueDefinition(definition: RuntimeDefinition))
//           if (definition.inputTypes.isEmpty && definition.outputType == testType) =>
//         fqn
//     }.toList
//     val testResults = UnitTesting.runTests(globals, dists, "Example Test Suite", tests: _*)
//     testResults
//   }

// import org.finos.morphir.naming.*
// import org.finos.morphir.ir.{Type as T, Value as V}
// import org.finos.morphir.ir.Literal.Lit
// import org.finos.morphir.ir.Value.{
//   Pattern,
//   TypedValue,
//   Value,
//   TypedDefinition as TypedValueDef,
//   USpecification as UValueSpec
// }
// import org.finos.morphir.ir.Type.{Field, Type, UType, USpecification as UTypeSpec}
// import org.finos.morphir.runtime.{
//   RTValue,
//   SDKConstructor,
//   SDKValue,
//   Utils,
//   Extractors,
//   Distributions,
//   TestResult,
//   MorphirRuntimeError
// }
// import org.finos.morphir.ir.printing.PrintIR
// import Extractors.*

// object UnitTesting {
//   val testType = T.reference("Morphir.Testing", "Test", "Test")

//   def runTest(
//       globals: GlobalDefs,
//       dists: Distributions,
//       testName: FQName
//   ): TestResult = {
//     val refValue  = Value.Reference.Typed(testType, testName)
//     val loop      = Loop(globals)
//     val evaluated = loop.loop(refValue, Store.empty)
//     evaluated match {
//       case RTValue.ConstructorResult(FQString("Morphir.Testing:Test:assert"), List(RTValue.Primitive.Boolean(true))) =>
//         TestResult.Success(testName.toString)
//       case RTValue.ConstructorResult(FQString("Morphir.Testing:Test:assert"), List(RTValue.Primitive.Boolean(false))) =>
//         formatFailure(globals, dists, testName.toString, refValue)
//       case RTValue.ConstructorResult(otherName, _) =>
//         TestResult.Failure(testName.toString, s"Unrecognized constructor result: $otherName")
//       case other => TestResult.Failure(testName.toString, s"Unrecognized value: ${other}")

//     }
//   }

//   private def formatFailure(
//       globals: GlobalDefs,
//       dists: Distributions,
//       testName: String,
//       ir: TypedValue
//   ): TestResult.Failure =
//     ir match {
//       case Value.Reference(_, name) =>
//         val found = dists.lookupValueDefinition(name) match {
//           case Left(value)  => throw value.withContext("Definition not found while formatting error")
//           case Right(value) => value
//         }
//         formatFailure(globals, dists, testName, found.body)
//       case Value.Apply(
//             _,
//             Value.Constructor(_, FQString("Morphir.Testing:Test:assert")),
//             Value.Apply(_, Value.Apply(_, _, actual), expected)
//           ) =>
//         val loop              = Loop(globals)
//         val actualEvaluated   = loop.loop(actual, Store.empty)
//         val expectedEvaluated = loop.loop(expected, Store.empty)
//         TestResult.Failure(
//           testName,
//           s"""
//              |\t\t($actual != $expected)
//              |\t\t${PrintIR(actualEvaluated)} != ${PrintIR(expectedEvaluated)}""".stripMargin
//         )
//       case other => TestResult.Failure(testName, s"($ir) evaluated to false")
//     }

//   def runTests(
//       globals: GlobalDefs,
//       dists: Distributions,
//       suiteName: String,
//       testNames: FQName*
//   ): TestResult = {
//     val results = testNames.toList.map(runTest(globals, dists, _))
//     TestResult.Suite(suiteName, results)
//   }

// }
