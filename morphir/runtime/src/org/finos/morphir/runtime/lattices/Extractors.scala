package org.finos.morphir.runtime.lattices
object Extractors{
    object Same{
        def unapply(left : LatticePoint, right : LatticePoint) : Option[LatticePoint] => {
            if (isSame(left, right)) Some(left) else None
        }
        def isSame(left : LatticePoint, right : LatticePoint) : Boolean => {
            (left, right) match {
                SimilarKids(kids) => kids.forall{case (left, right) => isSame(left, right)}
                other => false
            }
        }
    }
    object Kids{
        def unapplySeq(parent : LatticePoint) : Option[List[LatticePoint]] = {
            parent match {
                Top() => Some(List())
                Bottom() => Some(List())
                LList(elem) => Some(List(elem))
                LUnit() => Some(List())
                LTuple(elements)         => Some(elements)
                LFunction(arg, ret) => Some(List(arg, ret))
                LTVar(name, scope) => Some(List())
                LString() =>Some(List())
                LChar() =>Some(List())
                LBool() =>Some(List())
                LFloat() =>Some(List())
                LDecimal() =>Some(List())
                LLocalDate() =>Some(List())
                LLocalTime() =>Some(List())
                LConstructor(name, args) =>Some(args)
                Names(names) => Some(List())
                Named(inner, names) => Some(List(inner, names))
            }
        }
    }
    object SimilarKids{
        def unapplySeq(lParent: LatticePoint, rParent : LatticePoint) : Option[List[(LatticePoint, LatticePoint)]]= {
            (lParent, rParent) match {
                (l @ Kids(), r @ Kids()) if l == r => Some(List()) //Assumes we can use == on all non-recursive things
                (LList(lElem), LList(rRelem)) => Some(List((lElem, rElem)))
                (LTuple(lElems), LTuple(rElems)) 
                    if lElems.length == rElems.length =>
                    Some(lElems.zip(rElems))
                (LFunction(lArg, lRet), LFunction(rArg, rRet)) => Some(List((lArg, rArg), (lRet, rRet)))
                (LTVar(lName, lScope), LTVar(rName, rScope))
                    if (lName == rName && lScope == rScope) => Some(List())
                (LConstructor(lName, lArgs), LConstructor(rName, rArgs))
                    if (lName == rName) => Some(lArgs.zip(rArgs)) //Note - it should not be possible for these lengths to differ.
                (Named(lInner, lNames), Named(rInner, rNames)) => Some(List((lInner, rInner), (lNames, rNames)))
            }
        }
    }
}