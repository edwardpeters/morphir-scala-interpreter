package org.finos.morphir.datamodel.scratch

/**
 * This file is meant to simulate user code Requirements:
 * \- Must be able to provide an override for a "Leaf" concept
 * \- Must NOT have to provide overrides for any other concept (i.e., Tuple)
 */

object OtherDefault {

  given Default[FakeConcept.FakeString.type] with
    extension (c: FakeConcept.FakeString.type)
      def default: FakeData = FakeData.FakeString("Default")

  def otherDefault(c: FakeConcept): FakeData = Default.default(c)
}
