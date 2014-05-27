package de.uniulm.dds.leanimpl

import com.google.common.collect.IdentityInternable
import de.uniulm.dds.base._

/**
 * A wrapper for [[de.uniulm.dds.leanimpl.LeanDiagram]] that implements [[de.uniulm.dds.base.DecisionDiagram]] and has a reference to its [[de.uniulm.dds.leanimpl.LeanContext]].
 * <p/>
 * User: Felix
 * Date: 23.04.13
 * Time: 15:18
 */
private[leanimpl] final class DelegatingDiagram[V, T](val diagram: LeanDiagram[V, T], val context: LeanContext[V, T]) extends DecisionDiagram[V, T] with IdentityInternable {

  def apply(assignment: Map[Variable[V], V]): T = context.getValue(diagram, assignment)

  def transform[K](leafTransformation: T => K, innerNodeTransformation: (Variable[V], Map[V, K]) => K): K = context.transform(diagram, leafTransformation, innerNodeTransformation)

  def applyBinaryOperation(leafOperation: (T, T) => T, other: DecisionDiagram[V, T]): DecisionDiagram[V, T] = {
    require(other.context == context, "Argument " + other + " belongs to a different context.")
    other match {
      case delegatingDiagram: DelegatingDiagram[V, T] => context.applyBinaryOperation(diagram, leafOperation, delegatingDiagram.diagram)
    }
  }

  def applyUnaryOperation(leafOperation: T => T): DecisionDiagram[V, T] = context.applyUnaryOperation(diagram, leafOperation)

  def restrict(assignment: Map[Variable[V], V]): DecisionDiagram[V, T] = context.restrict(diagram, assignment)

  def internableEquals(o: AnyRef): Boolean = {
    o match {
      case that: DelegatingDiagram[V, T] => context == that.context && diagram == that.diagram
      case _ => false
    }
  }

  def internableHashCode(): Int = 31 * diagram.hashCode + context.hashCode

  def size: Int = context.innerNodeSet(diagram).size
}