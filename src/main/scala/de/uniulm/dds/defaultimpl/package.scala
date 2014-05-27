package de.uniulm.dds

import de.uniulm.dds.base.{Variable, Context}
import java.util.Comparator

/**
 * Contains the method for creating contexts
 * Created by Felix on 02.12.13.
 */
package object defaultimpl {
  /**
   * @param variableOrder the comparator that specifies variable order. For variables <code>a</code> and <code>b</code>,
   *                      <code>a</code> will always come before <code>b</code> in a diagram of this context iff
   *                      <code>variableOrder.compare(a, b) < 0</code>. The comparator must be consistent with equals.
   * @tparam V The type of the elements of variable domains
   * @tparam T The type of leaf values of the diagrams
   * @return the created context
   */
  def createContext[V, T](variableOrder: Comparator[Variable[V]]): Context[V, T] = new DefaultContext[V, T](variableOrder)
}
