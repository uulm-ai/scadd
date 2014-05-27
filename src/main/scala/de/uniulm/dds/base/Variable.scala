package de.uniulm.dds.base

import com.google.common.collect.IdentityInternable
import com.google.common.collect.IdentityInterners
import com.google.common.collect.Interner

/**
 * Represents a variable in a decision diagram.
 * <p/>
 * User: felix
 * Date: 27.03.13
 * Time: 16:12
 *
 * @tparam V the type of the elements of a variables domain.
 */
final class Variable[V] private(val name: String, val domain: Set[V]) extends IdentityInternable {
  require(domain.size >= 2, "Domain size must be two or more.")

  def internableEquals(o: AnyRef): Boolean = {
    o match {
      case variable: Variable[V] => domain == variable.domain && name == variable.name
      case _ => false
    }
  }

  def internableHashCode() = 31 * name.hashCode + domain.hashCode

  override def toString = name
}

object Variable {
  private final val interner: Interner[Variable[_]] = IdentityInterners.newWeakIdentityInterner()

  /**
   * Constructs a new variable.
   *
   * @param name   The name of the variable
   * @param domain The domain of the variable, i.e., the values it can take. Must be a finite set of cardinality of two or more.
   * @return the new variable
   */
  def apply[V](name: String, domain: Set[V]): Variable[V] = interner.asInstanceOf[Interner[Variable[V]]].intern(new Variable[V](name, domain))

  /**
   * Constructs a new boolean variable.
   *
   * @param name   The name of the variable
   * @return the new variable
   */
  def apply(name: String): Variable[Boolean] = Variable(name, Set(true, false))
}
