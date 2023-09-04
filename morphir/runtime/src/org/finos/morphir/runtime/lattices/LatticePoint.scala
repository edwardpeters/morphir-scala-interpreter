package org.finos.morphir.runtime.lattices

import org.finos.morphir.naming._
import org.finos.morphir.ir.{Type as T, Value as V}
import org.finos.morphir.ir.Value.{Value, Pattern, TypedValue, USpecification => UValueSpec}
import org.finos.morphir.ir.Type.{Type, UType, USpecification => UTypeSpec}

type TVarScope = String //TODO: Figure out what this is



sealed trait LatticePoint
object LatticePoint {
  case class Top()                                              extends LatticePoint
  case class Bottom()                                           extends LatticePoint
  case class LList(l: LatticePoint)                       extends LatticePoint
  case class LUnit()                                      extends LatticePoint
  case class LTuple(elements: List[LatticePoint])         extends LatticePoint
  case class LFunction(arg : LatticePoint, ret : LatticePoint) extends LatticePoint
  case class LTVar(name : Name, scope : TVarScope) //So here we do need scope...
  case class LString() extends LatticePoint
  case class LChar() extends LatticePoint
  case class LBool() extends LatticePoint
  case class LFloat() extends LatticePoint
  case class LDecimal() extends LatticePoint
  case class LLocalDate() extends LatticePoint
  case class LLocalTime() extends LatticePoint
  case class LRef(name : fqn, args : List[LatticePoint])

  /**
   * Does LConstructor even belong here? I kinda think not, but revisit after you code value constraints..
   */
  case class LConstructor(name : fqn, args : List[LatticePoint]) extends LatticePoint //Do we need branches?
  /**
   * So this is not an LP that maps directly to a type... but treating is as a child might make life easier anyway.
   * Aliases is completely disjoint from the rest of the lattice's ordering, but it can be nested beneath other things
   * Which I think (if we're careful) makes everything fine
   */
  case class Names(names : Set[LPName]) extends LatticePoint
  case class Named(inner : LatticePoint, names : LatticePoint) extends LatticePoint 

  /**
   * @param min
   *   for any record type R belonging to this LP: forall name in min.keys, R contains n and r(n) <= this.min(n)
   * @param max
   *   for any record type R beloning to this LP: forall name st max.keys contains name and r.key contains name, r(name)
   *   <= max(name)
   * @param extensible
   *   if false: for any record type R belonging to this LP: forall name in r.keys, r is in max
   */
  case class LatticeRecord(min: Map[Name, LatticePoint], max: Map[Name, LatticePoint], extensible: Boolean)
      extends LatticePoint
}

object LatticePoint{
    def fromUType(tpe : UType) => LatticePoint{
        tpe match {
            case Type.ExtensibleRecord((), name, fields) =>
            case Type.Function((), arg, ret) => LRecord(fromUType(arg), fromUType(ret))
            case Type.Record((), fields) => 
            case IntRef()       => ???
            case Int16Ref()     => ???
            case Int32Ref()     => ???
            case Int64Ref()     => ???
            case StringRef()    => ???
            case BoolRef()      => ???
            case CharRef()      => ???
            case FloatRef()     => ???
            case DecimalRef()   => ???
            case LocalDateRef() => ???
            case LocalTimeRef() => ???
            case ResultRef(err, ok) => LResult(fromUType(err), fromUType(ok))
            case ListRef(elementType) => LList(fromUType(elementType))
            case MaybeRef(elementType) => ???
            case DictRef(keyType, valType) => ???
            case SetRef(elementType) => ???
            case Type.Reference((), typeName, typeArgs) => ???
            case Type.Tuple((), elements) => LTuple(elements.toList.map(fromUType(_)))
            case Type.Unit(())           => LUnit()
            case Type.Variable((), name) => LTVar(name, ???) //TVAR_SCOPE : Here it is just from the type name...
        }
    }
    /**
     * Finds the greatest lattice descendant of left and right
     * This must hold the following three properties:
        gcd(left, right) <= left && gcd(left, right) <= right
        gcd(left, right) == gcd(right, left)
        forall other such that other <= left && other <= right, other <= gcd(left, right)
     */
    def gcd(left : LatticePoint, right : LatticePoint) : LatticePoint = {
        (left, right) match {
            case (Top(), other) => other
            case (other, Top()) => other
            case (Bottom(), _)  => Bottom() //Do we need to name our bottoms?
            case (_, Bottom())  => Bottom()
            case (Named (inner1, names1), Named(inner2, named2)) => 
                Named(gcd(inner1, inner2), gcd(names1, names2))
            case (Named(inner, names), other) => Named(gcd(inner, other), names)
            case (other, Named(inner, names), ) => Named(gcd(inner, other), names)
            ???
            case (other1, other2) => Bottom() //Most things in this lattice do not fit together...
        }
    }
    def partialOrdering(left : LatticePoint, right : LatticePoint) : PartialOrder = {
        import Extractors.*
        (left, right) match {
            case Same(_) => EQ
            case SimilarKids(pairs) => {
                pairs.foldLeft(EQ){case (acc, (innerLeft, innerRight) => 
                    (acc, partialOrdering(innerLeft, innerRight)) match {
                        case (EQ, other) => other
                        case (other, EQ) => other
                        case (NoOrder, _) => NoOrder
                        case (_, NoOrder) => NoOrder
                        case (GT, LT) => NoOrder
                        case (LT, GT) => NoOrder
                        case (GT, GT) => GT
                        case (LT, LT) => LT
                    }
                    )}
            }
        }
    }
    /**
     * Finds the least lattice ancestor of left and right
     * This must hold the following three properties:
        lca(left, right) >= left && lca(left, right) >= right
        lca(left, right) == lca((right, left)
        forall other such that other >= left && other >= right, other >= lca(left, right)
     */
    def lca(left : LatticePoint, right : LatticePoint) : LatticePoint = {
        (left, right) match {
            case (Top(), _) => Top()
            case (_, Top()) => Top()
            case (Bottom(), other)  => other
            case (other, Bottom())  => other
            case (Named (inner1, names1), Named(inner2, named2)) => 
                Named(lca(inner1, inner2), names1.intersect(names2))
            case (Named(inner, names), other) => lca(inner, other)
            case (other, Named(inner, names), ) => lca(inner, other)
            ???
            case (other1, other2) => Top() //Most things in this lattice do not fit together...
        }
    }
    def pick(lp : LatticePoint) : UType = {
        lp match{
            //For all leaf types there's only one answer...
            //For things with wiggle room... Generics?
        }
    }
}

/**
 * Okay extensible records are... oh, right, their own damn lattice points. That's what we worked out, right?
 */
