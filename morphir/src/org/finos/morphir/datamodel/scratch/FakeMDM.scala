package org.finos.morphir.datamodel.scratch

sealed trait FakeConcept
object FakeConcept {
  case object FakeString                                        extends FakeConcept
  case object FakeInt                                           extends FakeConcept
  case class FakeTuple(first: FakeConcept, second: FakeConcept) extends FakeConcept
}

sealed trait FakeData
object FakeData {
  case class FakeString(string: String)                   extends FakeData
  case class FakeInt(int: Int)                            extends FakeData
  case class FakeTuple(first: FakeData, second: FakeData) extends FakeData
}

trait Default[A <: FakeConcept]:
  extension (concept: A) def default: FakeData

object Default {
  def default(c: FakeConcept): FakeData =
    c match {
      case FakeConcept.FakeString   => FakeConcept.FakeString.default
      case FakeConcept.FakeInt      => FakeConcept.FakeInt.default
      case t: FakeConcept.FakeTuple => t.default
    }

  given Default[FakeConcept.FakeString.type] with
    extension (c: FakeConcept.FakeString.type)
      def default: FakeData = FakeData.FakeString("")

  given Default[FakeConcept.FakeInt.type] with
    extension (c: FakeConcept.FakeInt.type)
      def default: FakeData = FakeData.FakeInt(0)

  given Default[FakeConcept.FakeTuple] with
    extension (c: FakeConcept.FakeTuple)
      def default: FakeData = FakeData.FakeTuple(Default.default(c.first), Default.default(c.second))
}
