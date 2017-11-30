package de.uniulm.dds.leanimpl

import de.uniulm.dds.base._

/**
 * Tests for the lean implementation.
 *
 * User: Felix
 * Date: 23.04.13
 * Time: 17:12
 */
class LeanImplTests extends AbstractTests {
  def createContext[V, T](ordering: Ordering[Variable[V]] = lexicographicOrdering[V]): Context[V, T] = Context(Parameters[V, T](ordering))
}