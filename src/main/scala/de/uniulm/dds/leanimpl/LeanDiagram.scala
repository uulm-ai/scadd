package de.uniulm.dds.leanimpl

import com.google.common.collect.IdentityInternable
import de.uniulm.dds.base.Variable
import java.util

/**
 * Common interface for [[de.uniulm.dds.leanimpl.InnerNode]] and [[de.uniulm.dds.leanimpl.Leaf]] classes.
 * <p/>
 * User: Felix
 * Date: 23.04.13
 * Time: 15:19
 */
private[leanimpl] sealed trait LeanDiagram[+V, T] extends IdentityInternable

/**
 * A leaf node of a decision diagram.
 * <p/>
 * User: Felix
 * Date: 23.04.13
 * Time: 15:22
 */
private[leanimpl] final class Leaf[T](val value: T) extends LeanDiagram[Nothing, T] {
  def internableEquals(o: AnyRef): Boolean = {
    o match {
      case leaf: Leaf[T] => value == leaf.value
      case _ => false
    }
  }

  def internableHashCode(): Int = value.hashCode
}

private[leanimpl] final class InnerNode[V, T](val variable: Variable[V], val children: Array[LeanDiagram[V, T]]) extends LeanDiagram[V, T] {

  def internableEquals(o: AnyRef): Boolean = {
    o match {
      case innerNode: InnerNode[V, T] => util.Arrays.equals(children.asInstanceOf[Array[AnyRef]], innerNode.children.asInstanceOf[Array[AnyRef]]) && variable == innerNode.variable
      case _ => false
    }
  }

  def internableHashCode(): Int = 31 * variable.hashCode + util.Arrays.hashCode(children.asInstanceOf[Array[AnyRef]])
}
