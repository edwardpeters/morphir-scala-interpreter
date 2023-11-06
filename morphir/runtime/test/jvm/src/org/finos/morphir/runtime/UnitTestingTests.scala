package org.finos.morphir.runtime

import org.finos.morphir.datamodel.*
import org.finos.morphir.datamodel.Util.*
import org.finos.morphir.ir.Type
import org.finos.morphir.naming.*
import org.finos.morphir.runtime.environment.MorphirEnv
import org.finos.morphir.runtime.TestResult as MorphirTestResult
import org.finos.morphir.testing.MorphirBaseSpec
import zio.test.*
import zio.test.TestAspect.{ignore, tag}
import zio.{Console, ZIO, ZLayer}

object UnitTestingTests extends MorphirBaseSpec {
  val morphirRuntimeLayer: ZLayer[Any, Throwable, TypedMorphirRuntime] =
    ZLayer(for {
      frameworkIRFilepath <- ZIO.succeed(
        os.pwd / "examples" / "morphir-elm-projects" / "evaluator-unit-test-framework" / "test-framework" / "morphir-ir.json"
      )
      _             <- Console.printLine(s"Loading unit test framework from $frameworkIRFilepath")
      frameworkDist <- EvaluationLibrary.loadDistributionFromFileZIO(frameworkIRFilepath.toString)
      examplesIRFilepath <- ZIO.succeed(
        os.pwd / "examples" / "morphir-elm-projects" / "evaluator-unit-test-framework" / "unit-test-examples" / "morphir-ir.json"
      )
      _            <- Console.printLine(s"Loading examples from $examplesIRFilepath")
      examplesDist <- EvaluationLibrary.loadDistributionFromFileZIO(examplesIRFilepath.toString)
    } yield MorphirRuntime.quick(frameworkDist, examplesDist))

  def deriveData(input: Any): Data =
    input match {
      // If the data is already derived, just use it!
      case alreadyData: Data       => alreadyData
      case u: Unit                 => Deriver.toData(u)
      case b: Boolean              => Deriver.toData(b)
      case i: Int                  => Deriver.toData(i)
      case s: String               => Deriver.toData(s)
      case ld: java.time.LocalDate => Deriver.toData(ld)
      case lt: java.time.LocalTime => Deriver.toData(lt)
      case list: List[_] =>
        val mapped = list.map(deriveData(_))
        Data.List(mapped.head, mapped.tail: _*)
      case map: Map[_, _] =>
        val pairs = map.toList.map { case (key, value) => (deriveData(key), deriveData(value)) }
        Data.Map(pairs.head, pairs.tail: _*)
      case Some(a: Any)           => Data.Optional.Some(deriveData(a))
      case (first, second)        => Data.Tuple(deriveData(first), deriveData(second))
      case (first, second, third) => Data.Tuple(deriveData(first), deriveData(second), deriveData(third))
      case e: Either[_, _] => throw new Exception(
          s"Couldn't derive $e (Hint: I can't tell what the other side of the either would be. Use Data constructors directly instead."
        )
      case other => throw new Exception(s"Couldn't derive $other")
    }

  def checkEvaluation(
      packageName: String,
      moduleName: String,
      functionName: String
  )(expected: => Data): ZIO[TypedMorphirRuntime, Throwable, TestResult] =
    runTest(packageName, moduleName, functionName, List()).map { actual =>
      assertTrue(actual == expected)
    }

  def runTest(
      packageName: String,
      moduleName: String,
      functionName: String,
      values: List[Any]
  ): ZIO[TypedMorphirRuntime, Throwable, Data] =
    ZIO.serviceWithZIO[TypedMorphirRuntime] { runtime =>
      val data = values.map(deriveData(_))
      runTestMDM(packageName, moduleName, functionName, data)
    }

  def checkEvaluation(
      packageName: String,
      moduleName: String,
      functionName: String,
      values: List[Any]
  )(expected: => Data): ZIO[TypedMorphirRuntime, Throwable, TestResult] =
    runTest(packageName, moduleName, functionName, values).map { actual =>
      assertTrue(actual == expected)
    }

  def testEval(label: String)(packageName: String, moduleName: String, functionName: String)(expected: => Data) =
    test(label) {
      checkEvaluation(packageName, moduleName, functionName)(expected)
    }

  def testEval(label: String)(
      packageName: String,
      moduleName: String,
      functionName: String,
      value: Any
  )(expected: => Data) =
    test(label) {
      checkEvaluation(packageName, moduleName, functionName, List(value))(expected)
    }

  def runTestMDM(
      packageName: String,
      moduleName: String,
      functionName: String,
      data: List[Data]
  ): ZIO[TypedMorphirRuntime, Throwable, Data] =
    ZIO.serviceWithZIO[TypedMorphirRuntime] { runtime =>
      val fullName = s"$packageName:$moduleName:$functionName"
      if (data.isEmpty)
        runtime.evaluate(
          FQName.fromString(fullName)
        )
          .provideEnvironment(MorphirEnv.live)
          .toZIOWith(RTExecutionContext.typeChecked)
      else
        runtime.evaluate(FQName.fromString(fullName), data.head, data.tail: _*)
          .provideEnvironment(MorphirEnv.live)
          .toZIOWith(RTExecutionContext.typeChecked)
    }

  def runUnitTests(
  ): ZIO[TypedMorphirRuntime, Throwable, MorphirTestResult] =
    ZIO.serviceWithZIO[TypedMorphirRuntime] { runtime =>
      runtime.test()
        .provideEnvironment(MorphirEnv.live)
        .toZIOWith(RTExecutionContext.typeChecked)
    }

  def checkUnitTesting(): ZIO[TypedMorphirRuntime, Throwable, TestResult] =
    runUnitTests().map { actual =>
      assertTrue(actual.toString == "")
    }

  def testTests(label: String) = test(label) {
    checkUnitTesting()
  }

  def spec =
    suite("Unit Testing Direct Tests")(
//      suite("Basic Tests")(
//        testEval("foo")("Examples.UnitTesting", "SomeCode", "foo", 1)(Data.Int(67)),
//
//        testEval("fooTest")("Examples.UnitTesting", "SomeTests", "fooTest")(Data.Int(67))
//      ),
      suite("Basic Tests")(
        testTests("Basic unit testing")
      )
    ).provideLayerShared(morphirRuntimeLayer)

}
