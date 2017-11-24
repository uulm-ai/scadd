package de.uniulm.dds.leanimpl

import java.util.Comparator

import de.uniulm.dds.base.Variable

/**
  * The parameter type for contexts in defaultimpl
  * Created by Felix on 03.06.2014.
  *
  * @param variableOrder     the comparator that specifies variable order. For variables <code>a</code> and <code>b</code>,
  *                          <code>a</code> will always come before <code>b</code> in a diagram of this context iff
  *                          <code>variableOrder.compare(a, b) < 0</code>. The comparator must be consistent with equals.
  * @param restrictCacheSize the size of the restrict cache
  * @param binaryOpCacheSize the size of the binary operation cache
  * @param unaryOpCacheSize  the size of the unary operation cache
  * @tparam V The type of the elements of variable domains
  * @tparam T The type of leaf values of the diagrams
  */
case class Parameters[V, T](variableOrder: Comparator[Variable[V]], restrictCacheSize: Int = 1000000, binaryOpCacheSize: Int = 1000000, unaryOpCacheSize: Int = 1000000)
