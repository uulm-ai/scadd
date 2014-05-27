package de.uniulm.dds.defaultimpl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.collect.IdentityInterners
import com.google.common.collect.Interner
import de.uniulm.dds.base._
import java.util.Comparator

/**
 * The default context implementation. Ensures diagram uniqueness and supplies standard operations, but does not
 * implement caching of operation results.
 * <p/>
 * User: felix
 * Date: 26.03.13
 * Time: 14:59
 * @tparam V The type of the elements of variable domains
 * @tparam T The type of leaf values of the diagrams
 * @param variableOrder the comparator that specifies variable order. For variables <code>a</code> and <code>b</code>,
 *                      <code>a</code> will always come before <code>b</code> in a diagram of this context iff
 *                      <code>variableOrder.compare(a, b) < 0</code>. The comparator must be consistent with equals.
 * @param restrictCacheSize the size of the restrict cache
 * @param binaryOpCacheSize the size of the binary operation cache
 * @param unaryOpCacheSize the size of the unary operation cache
 */
private[defaultimpl] class DefaultContext[V, T] private[defaultimpl](val variableOrder: Comparator[Variable[V]], restrictCacheSize: Int, binaryOpCacheSize: Int, unaryOpCacheSize: Int) extends Context[V, T] {

  val interner: Interner[DefaultDiagram[V, T]] = IdentityInterners.newWeakIdentityInterner()
  val restrictCache: Cache[(DefaultDiagram[V, T], Map[Variable[V], V]), DefaultDiagram[V, T]] = CacheBuilder.newBuilder.maximumSize(restrictCacheSize).build()
  val applyBinaryOperationCache: Cache[(DefaultDiagram[V, T], (T, T) => T, DefaultDiagram[V, T]), DefaultDiagram[V, T]] = CacheBuilder.newBuilder.maximumSize(binaryOpCacheSize).build()
  val applyUnaryOperationCache: Cache[(DefaultDiagram[V, T], (T) => T), DefaultDiagram[V, T]] = CacheBuilder.newBuilder.maximumSize(unaryOpCacheSize).build()

  def getConstantDiagram(value: T): DefaultDiagram[V, T] = interner.intern(new Leaf[V, T](value, this))

  def getSimpleDiagram(variable: Variable[V], children: Map[V, T]): DecisionDiagram[V, T] = {
    require(children.keySet == variable.domain, children + " must exactly contain one mapping for each value of " + variable + ".")
    getDiagramUnsafe(variable, children.mapValues(getConstantDiagram))
  }

  private[defaultimpl] def getDiagramUnsafe(variable: Variable[V], children: Map[V, DefaultDiagram[V, T]]): DefaultDiagram[V, T] = {
    val firstChild: DefaultDiagram[V, T] = children(children.keySet.iterator.next())
    if (children.values.map(_ eq firstChild).reduce(_ && _)) firstChild
    else interner.intern(new InnerNode[V, T](variable, children, this))
  }
}
