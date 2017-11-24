package de.uniulm.dds.leanimpl

import de.uniulm.dds.base._
import de.uniulm.identityinternable.IdentityInternable

/**
  * A wrapper for [[de.uniulm.dds.leanimpl.LeanDiagram]] that implements [[de.uniulm.dds.base.DecisionDiagram]] and has a reference to its [[de.uniulm.dds.leanimpl.LeanContext]].
  * <p/>
  * User: Felix
  * Date: 23.04.13
  * Time: 15:18
  */
private[leanimpl] final case class DelegatingDiagram[V, T](diagram: LeanDiagram[V, T], context: LeanContext[V, T]) extends DecisionDiagram[V, T] with IdentityInternable {

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

  def size: Int = context.innerNodeSet(diagram).size
}