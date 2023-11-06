package org.finos.morphir.runtime

sealed trait TestResult {
  def name: String

  def isFailure: Boolean
}

object TestResult {

  case class Success(name: String) extends TestResult {
    override def isFailure: Boolean = false
    override def toString           = s"$name: SUCCESS"
  }

  case class Failure(name: String, message: String) extends TestResult {
    override def isFailure: Boolean = true
    override def toString           = s"$name: FAILED - $message"
  }

  case class Suite(name: String, tests: List[TestResult]) extends TestResult {
    override def isFailure: Boolean = !tests.forall(_.isFailure)
    override def toString =
      s"""
         |$name: ${if (isFailure) "FAILED" else "SUCCESS"}
         |\t${tests.map(_.toString).mkString("\n\t")}
         |""".stripMargin
  }

  val empty = Suite("Empty Test Suite", List())
}
