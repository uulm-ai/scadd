package de.uniulm.dds.defaultimpl

import de.uniulm.dds.base._

/**
 * Tests for the default implementation
 *
 * User: Felix
 * Date: 23.04.13
 * Time: 17:11
 */
class DefaultImplTests extends AbstractTests {
  def createContext[V, T](ordering: Ordering[Variable[V]] = lexicographicOrdering[V]): Context[V, T] = Context(Parameters[V, T](ordering))
}