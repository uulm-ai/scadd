package de.uniulm.dds.base

import de.uniulm.identityinternable.{IdentityInternable, IdentityInterner}


/**
  * Represents a variable in a decision diagram.
  * <p/>
  * User: felix
  * Date: 27.03.13
  * Time: 16:12
  *
  * @tparam V the type of the elements of a variables domain.
  */
final case class Variable[V] private(name: String, domain: Set[V]) extends IdentityInternable {
  require(domain.size >= 2, "Domain size must be two or more.")

  override def toString: String = name

  /**
    * Creates an indicator diagram that map `indicatedValue` to 1 and all other values to 0.
    *
    * @param indicatedValue the indicated value
    * @param context        the context to use for constructing the indicator diagram
    * @param n              the numeric
    * @tparam T the number type
    * @return the indicator diagram
    */
  def indicator[T](indicatedValue: V)(implicit context: Context[V, T], n: Numeric[T]): DecisionDiagram[V, T] =
    DecisionDiagram(this, domain.map[(V, T), Map[V, T]](x => if (x == indicatedValue) x -> n.one else x -> n.zero)(collection.breakOut))
}

object Variable {
  private final val interner: IdentityInterner[Variable[_]] = IdentityInterner.newWeakIdentityInterner()

  /**
    * Constructs a new variable.
    *
    * @param name   The name of the variable
    * @param domain The domain of the variable, i.e., the values it can take. Must be a finite set of cardinality of two or more.
    * @return the new variable
    */
  def apply[V](name: String, domain: Set[V]): Variable[V] = interner.asInstanceOf[IdentityInterner[Variable[V]]].intern(new Variable[V](name, domain))

  /**
    * Constructs a new boolean variable.
    *
    * @param name The name of the variable
    * @return the new variable
    */
  def apply(name: String): Variable[Boolean] = Variable(name, Set(true, false))
}
