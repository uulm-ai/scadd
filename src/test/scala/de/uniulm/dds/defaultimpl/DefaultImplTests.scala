package de.uniulm.dds.defaultimpl

import de.uniulm.dds.base.AbstractTests
import de.uniulm.dds.base.Context
import de.uniulm.dds.base.Variable
import java.util.Comparator
import de.uniulm.dds.defaultimpl

/**
 * Tests for the default implementation
 *
 * User: Felix
 * Date: 23.04.13
 * Time: 17:11
 */
class DefaultImplTests extends AbstractTests {
  def createContext[V, T](variableOrder: Comparator[Variable[V]]): Context[V, T] = defaultimpl.createContext(variableOrder)
}