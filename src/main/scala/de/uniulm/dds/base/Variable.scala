package de.uniulm.dds.base


/**
  * Represents a variable in a decision diagram.
  * <p/>
  * User: felix
  * Date: 27.03.13
  * Time: 16:12
  *
  * @tparam V the type of the elements of a variables domain.
  */
final class Variable[V] private[base](val name: String, val domain: Set[V]) {
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
  /**
    * Constructs a new variable.
    *
    * @param name    The name of the variable
    * @param domain  The domain of the variable, i.e., the values it can take. Must be a finite set of cardinality of two or more.
    * @param context the (possibly implicitly given) context that creates the actual variable
    * @return the new variable
    */
  def apply[V, T](name: String, domain: Set[V])(implicit context: Context[V, T]): Variable[V] = context.getVariable(name, domain)
}
