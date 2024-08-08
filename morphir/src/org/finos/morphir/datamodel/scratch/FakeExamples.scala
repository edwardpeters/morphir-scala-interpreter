package org.finos.morphir.datamodel.scratch

object FakeExamples {
  def fakeTupleConcept: FakeConcept = FakeConcept.FakeTuple(FakeConcept.FakeInt, FakeConcept.FakeString)
  def fakeTupleData: FakeData       = FakeData.FakeTuple(FakeData.FakeInt(0), FakeData.FakeString(""))
  def otherFakeTupleData: FakeData  = FakeData.FakeTuple(FakeData.FakeInt(0), FakeData.FakeString("Default"))
}
