package de.uniulm.dds.leanimpl

import de.uniulm.dds.base.Variable
import de.uniulm.identityinternable.IdentityInternable

import scala.collection.mutable

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
private[leanimpl] final case class Leaf[T](value: T) extends LeanDiagram[Nothing, T]

private[leanimpl] final case class InnerNode[V, T](variable: Variable[V], children: mutable.WrappedArray[LeanDiagram[V, T]]) extends LeanDiagram[V, T]
