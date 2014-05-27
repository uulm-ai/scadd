package de.uniulm.dds.leanimpl

import java.util.Comparator
import de.uniulm.dds.base.{Context, Variable, AbstractTests}
import de.uniulm.dds.leanimpl

/**
 * Tests for the lean implementation.
 *
 * User: Felix
 * Date: 23.04.13
 * Time: 17:12
 */
class LeanImplTests extends AbstractTests {
  def createContext[V, T](variableOrder: Comparator[Variable[V]]): Context[V, T] = leanimpl.createContext(variableOrder)
}