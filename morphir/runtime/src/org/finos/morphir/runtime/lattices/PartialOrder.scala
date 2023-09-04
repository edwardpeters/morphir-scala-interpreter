package org.finos.morphir.runtime.lattices

sealed trait PartialOrder

object PartialOrder{
    case object LT extends PartialOrder
    case object GT extends PartialOrder
    case object EQ extends PartialOrder
    case object NoOrder extends PartialOrder
}