package de.uniulm.dds.defaultimpl

import com.google.common.collect.IdentityInternable
import de.uniulm.dds.base.DecisionDiagram
import de.uniulm.dds.base.Variable
import java.util.concurrent.Callable

/**
 * Extension of the [[de.uniulm.dds.defaultimpl.DefaultDiagram]] interface for the default implementation.
 * <p/>
 * User: Felix
 * Date: 20.04.13
 * Time: 22:51
 */
private[defaultimpl] sealed trait DefaultDiagram[V, T] extends DecisionDiagram[V, T] with IdentityInternable {

  def context: DefaultContext[V, T]

  def applyBinaryOperation(leafOperation: (T, T) => T, other: DecisionDiagram[V, T]): DecisionDiagram[V, T] = {
    require(other.context == context, "Argument " + other + " belongs to a different context.")
    other match {
      case defaultDiagram: DefaultDiagram[V, T] => applyBinaryOperationInternal(leafOperation, defaultDiagram)
    }
  }

  def applyBinaryOperationInternal(leafOperation: (T, T) => T, other: DefaultDiagram[V, T]): DefaultDiagram[V, T]

  def applyUnaryOperation(leafOperation: T => T): DefaultDiagram[V, T]

  def restrict(assignment: Map[Variable[V], V]): DefaultDiagram[V, T]

  def innerNodeSet: Set[DefaultDiagram[V, T]]
}

/**
 * An inner node of a decision diagram.
 * <p/>
 * User: felix
 * Date: 26.03.13
 * Time: 15:53
 */
private[defaultimpl] final class InnerNode[V, T](val variable: Variable[V], val children: Map[V, DefaultDiagram[V, T]], val context: DefaultContext[V, T]) extends DefaultDiagram[V, T] {

  def apply(assignment: Map[Variable[V], V]): T = children(assignment(variable))(assignment)

  def internableEquals(o: AnyRef): Boolean = {
    o match {
      case innerNode: InnerNode[V, T] => children == innerNode.children && context == innerNode.context && variable == innerNode.variable
      case _ => false
    }
  }

  def internableHashCode(): Int = 31 * (31 * variable.hashCode + children.hashCode) + context.hashCode

  // todo: intermediate result caching?
  def transform[K](leafTransformation: T => K, innerNodeTransformation: (Variable[V], Map[V, K]) => K): K =
    innerNodeTransformation(variable, children.mapValues(_.transform(leafTransformation, innerNodeTransformation)))

  def applyBinaryOperationInternal(leafOperation: (T, T) => T, other: DefaultDiagram[V, T]): DefaultDiagram[V, T] = {
    context.applyBinaryOperationCache.get((this, leafOperation, other), new Callable[DefaultDiagram[V, T]] {
      def call: DefaultDiagram[V, T] = {
        other match {
          case leaf: Leaf[V, T] => context.getDiagramUnsafe(variable, children.mapValues(_.applyBinaryOperationInternal(leafOperation, other)))
          case inner: InnerNode[V, T] =>
            val comparisonResult: Int = context.parameters.variableOrder.compare(variable, inner.variable)
            if (comparisonResult < 0) {
              context.getDiagramUnsafe(variable, children.mapValues(_.applyBinaryOperationInternal(leafOperation, other)))
            }
            else if (comparisonResult > 0) {
              context.getDiagramUnsafe(inner.variable, inner.children.mapValues(applyBinaryOperationInternal(leafOperation, _)))
            }
            else {
              context.getDiagramUnsafe(inner.variable, variable.domain.map(x => x -> children(x).applyBinaryOperationInternal(leafOperation, inner.children(x)))(collection.breakOut))
            }
        }
      }
    })
  }

  def applyUnaryOperation(leafOperation: T => T): DefaultDiagram[V, T] = {
    context.applyUnaryOperationCache.get((this, leafOperation), new Callable[DefaultDiagram[V, T]] {
      def call: DefaultDiagram[V, T] = context.getDiagramUnsafe(variable, children.mapValues(_.applyUnaryOperation(leafOperation)))
    })
  }

  def restrict(assignment: Map[Variable[V], V]): DefaultDiagram[V, T] = {
    context.restrictCache.get((this, assignment), new Callable[DefaultDiagram[V, T]] {
      def call: DefaultDiagram[V, T] = {
        if (assignment.contains(variable)) children(assignment(variable)).restrict(assignment)
        else context.getDiagramUnsafe(variable, children.mapValues(_.restrict(assignment)))
      }
    })
  }

  def size: Int = innerNodeSet.size

  def innerNodeSet: Set[DefaultDiagram[V, T]] = Set(this) ++ children.values.flatMap(_.innerNodeSet)
}

/**
 * A leaf node of a decision diagram.
 * <p/>
 * User: felix
 * Date: 26.03.13
 * Time: 15:10
 */
private[defaultimpl] final class Leaf[V, T](val value: T, val context: DefaultContext[V, T]) extends DefaultDiagram[V, T] {

  def apply(assignment: Map[Variable[V], V]): T = value

  def internableEquals(o: AnyRef): Boolean = {
    o match {
      case leaf: Leaf[V, T] => context == leaf.context && value == leaf.value
      case _ => false
    }
  }

  def internableHashCode(): Int = 31 * value.hashCode + context.hashCode

  def transform[K](leafTransformation: T => K, innerNodeTransformation: (Variable[V], Map[V, K]) => K): K = leafTransformation(value)

  def applyBinaryOperationInternal(leafOperation: (T, T) => T, other: DefaultDiagram[V, T]): DefaultDiagram[V, T] = {
    other match {
      case leaf: Leaf[V, T] => context.getConstantDiagram(leafOperation(value, leaf.value))
      case inner: InnerNode[V, T] =>
        context.applyBinaryOperationCache.get((this, leafOperation, other), new Callable[DefaultDiagram[V, T]] {
          def call: DefaultDiagram[V, T] = {
            context.getDiagramUnsafe(inner.variable, inner.children.mapValues(applyBinaryOperationInternal(leafOperation, _)))
          }
        })
    }
  }

  def applyUnaryOperation(leafOperation: T => T): DefaultDiagram[V, T] = context.getConstantDiagram(leafOperation(value))

  def restrict(assignment: Map[Variable[V], V]): DefaultDiagram[V, T] = this

  def size: Int = 0

  def innerNodeSet: Set[DefaultDiagram[V, T]] = Set()
}