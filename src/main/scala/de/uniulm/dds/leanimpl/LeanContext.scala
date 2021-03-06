package de.uniulm.dds.leanimpl

import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}
import de.uniulm.dds.base._
import de.uniulm.identityinternable.IdentityInterner

import scala.collection.mutable

/**
  * The context implementation.
  * <p/>
  * User: Felix
  * Date: 23.04.13
  * Time: 15:21
  *
  * @param parameters the configuration parameters
  * @tparam V The type of the elements of variable domains
  * @tparam T The type of leaf values of the diagrams
  */
private[leanimpl] class LeanContext[V, T](parameters: Parameters[V, T]) extends Context[V, T] {

  private final val delegatingDiagramInterner: IdentityInterner[DelegatingDiagram[V, T]] = IdentityInterner.newWeakIdentityInterner()
  private final val leanDiagramInterner: IdentityInterner[LeanDiagram[V, T]] = IdentityInterner.newWeakIdentityInterner()
  private final val variableValueIndices: LoadingCache[Variable[V], Map[V, Int]] = CacheBuilder.newBuilder.build(new CacheLoader[Variable[V], Map[V, Int]] {
    def load(key: Variable[V]): Map[V, Int] = key.domain.zipWithIndex[V, Map[V, Int]](collection.breakOut)
  })

  private final val restrictCache: LoadingCache[(LeanDiagram[V, T], Map[Variable[V], V]), LeanDiagram[V, T]] = CacheBuilder.newBuilder.maximumSize(parameters.restrictCacheSize).build(new CacheLoader[(LeanDiagram[V, T], Map[Variable[V], V]), LeanDiagram[V, T]] {
    def load(key: (LeanDiagram[V, T], Map[Variable[V], V])): LeanDiagram[V, T] = {
      key match {
        case (leaf: Leaf[T], _) => leaf
        case (inner: InnerNode[V, T], assignment) if assignment.contains(inner.variable) =>
          restrictCache.getUnchecked((inner.children(variableValueIndices.getUnchecked(inner.variable)(assignment(inner.variable))), assignment))
        case (inner: InnerNode[V, T], assignment) =>
          getDiagramInternal(inner.variable, inner.children.map(x => restrictCache.getUnchecked((x, assignment))))
      }
    }
  })
  private final val applyBinaryOperationCache: LoadingCache[(LeanDiagram[V, T], (T, T) => T, LeanDiagram[V, T]), LeanDiagram[V, T]] = CacheBuilder.newBuilder.maximumSize(parameters.binaryOpCacheSize).build(new CacheLoader[(LeanDiagram[V, T], (T, T) => T, LeanDiagram[V, T]), LeanDiagram[V, T]] {
    def load(key: (LeanDiagram[V, T], (T, T) => T, LeanDiagram[V, T])): LeanDiagram[V, T] = {
      key match {
        case (leftLeaf: Leaf[T], operation, rightLeaf: Leaf[T]) =>
          getConstantDiagramInternal(operation(leftLeaf.value, rightLeaf.value))
        case (leftLeaf: Leaf[T], operation, rightInner: InnerNode[V, T]) =>
          getDiagramInternal(rightInner.variable, rightInner.children.map(x => applyBinaryOperationCache.getUnchecked((leftLeaf, operation, x))))
        case (leftInner: InnerNode[V, T], operation, rightLeaf: Leaf[T]) =>
          getDiagramInternal(leftInner.variable, leftInner.children.map(x => applyBinaryOperationCache.getUnchecked((x, operation, rightLeaf))))
        case (leftInner: InnerNode[V, T], operation, rightInner: InnerNode[V, T]) if parameters.variableOrder.compare(leftInner.variable, rightInner.variable) < 0 =>
          getDiagramInternal(leftInner.variable, leftInner.children.map(x => applyBinaryOperationCache.getUnchecked((x, operation, rightInner))))
        case (leftInner: InnerNode[V, T], operation, rightInner: InnerNode[V, T]) if parameters.variableOrder.compare(leftInner.variable, rightInner.variable) > 0 =>
          getDiagramInternal(rightInner.variable, rightInner.children.map(x => applyBinaryOperationCache.getUnchecked(leftInner, operation, x)))
        case (leftInner: InnerNode[V, T], operation, rightInner: InnerNode[V, T]) =>
          getDiagramInternal(rightInner.variable, leftInner.children.zip(rightInner.children).map({ case (a, b) => applyBinaryOperationCache.getUnchecked(a, operation, b) }))
      }
    }
  })

  private final val applyUnaryOperationCache: LoadingCache[(LeanDiagram[V, T], T => T), LeanDiagram[V, T]] = CacheBuilder.newBuilder.maximumSize(parameters.unaryOpCacheSize).build(new CacheLoader[(LeanDiagram[V, T], T => T), LeanDiagram[V, T]] {
    def load(key: (LeanDiagram[V, T], T => T)): LeanDiagram[V, T] = {
      key match {
        case (leaf: Leaf[T], operation) => getConstantDiagramInternal(operation(leaf.value))
        case (inner: InnerNode[V, T], operation) => getDiagramInternal(inner.variable, inner.children.map(x => applyUnaryOperationCache.getUnchecked((x, operation))))
      }
    }
  })

  def getConstantDiagram(value: T): DecisionDiagram[V, T] = delegatingDiagramInterner.intern(DelegatingDiagram[V, T](getConstantDiagramInternal(value), this))

  private def getConstantDiagramInternal(value: T): LeanDiagram[V, T] = leanDiagramInterner.intern(Leaf[T](value))

  def getSimpleDiagram(variable: Variable[V], children: Map[V, T]): DecisionDiagram[V, T] = {
    require(children.keySet == variable.domain, children + " must exactly contain one mapping for each value of " + variable + ".")
    val indices: Map[V, Int] = variableValueIndices.getUnchecked(variable)
    val array: mutable.WrappedArray[LeanDiagram[V, T]] = new Array[LeanDiagram[V, T]](children.size)
    for ((v, t) <- children) {
      array(indices(v)) = getConstantDiagramInternal(t)
    }
    delegatingDiagramInterner.intern(DelegatingDiagram[V, T](getDiagramInternal(variable, array), this))
  }

  private def getDiagramInternal(variable: Variable[V], children: mutable.WrappedArray[LeanDiagram[V, T]]): LeanDiagram[V, T] = {
    children.find(_ != children(0)).fold(children(0))(_ => leanDiagramInterner.intern(InnerNode[V, T](variable, children))) // tail forall head?
  }

  // todo: intermediate result caching?
  private[leanimpl] def transform[K](diagram: LeanDiagram[V, T], leafTransformation: T => K, innerNodeTransformation: (Variable[V], Map[V, K]) => K): K = {
    diagram match {
      case leaf: Leaf[T] => leafTransformation(leaf.value)
      case inner: InnerNode[V, T] => innerNodeTransformation(inner.variable, inner.variable.domain.map(x => (x, transform(inner.children(variableValueIndices.get(inner.variable)(x)), leafTransformation, innerNodeTransformation)))(collection.breakOut))
    }
  }

  private[leanimpl] def getValue(diagram: LeanDiagram[V, T], assignment: Map[Variable[V], V]): T = {
    diagram match {
      case leaf: Leaf[T] => leaf.value
      case inner: InnerNode[V, T] =>
        require(assignment.contains(inner.variable), "assignment" + assignment + " contain variable " + inner.variable + ".")
        getValue(inner.children(variableValueIndices.getUnchecked(inner.variable)(assignment(inner.variable))), assignment)
    }
  }

  private[leanimpl] def applyBinaryOperation(left: LeanDiagram[V, T], leafOperation: (T, T) => T, right: LeanDiagram[V, T]): DecisionDiagram[V, T] =
    delegatingDiagramInterner.intern(DelegatingDiagram[V, T](applyBinaryOperationCache.getUnchecked((left, leafOperation, right)), this))

  private[leanimpl] def applyUnaryOperation(diagram: LeanDiagram[V, T], leafOperation: T => T): DecisionDiagram[V, T] =
    delegatingDiagramInterner.intern(DelegatingDiagram[V, T](applyUnaryOperationCache.getUnchecked((diagram, leafOperation)), this))

  private[leanimpl] def restrict(diagram: LeanDiagram[V, T], assignment: Map[Variable[V], V]): DecisionDiagram[V, T] =
    delegatingDiagramInterner.intern(DelegatingDiagram[V, T](restrictCache.getUnchecked((diagram, assignment)), this))

  private[leanimpl] def innerNodeSet(diagram: LeanDiagram[V, T]): Set[LeanDiagram[V, T]] =
    diagram match {
      case _: Leaf[T] => Set()
      case inner: InnerNode[V, T] => Set(diagram) ++ inner.children.flatMap(innerNodeSet)
    }
}
