package de.uniulm.dds.defaultimpl

import com.google.common.cache.{Cache, CacheBuilder}
import de.uniulm.dds.base._
import de.uniulm.identityinternable.IdentityInterner

/**
  * The default context implementation. Ensures diagram uniqueness and supplies standard operations, but does not
  * implement caching of operation results.
  * <p/>
  * User: felix
  * Date: 26.03.13
  * Time: 14:59
  *
  * @tparam V The type of the elements of variable domains
  * @tparam T The type of leaf values of the diagrams
  * @param parameters the configuration parameters
  */
private[defaultimpl] class DefaultContext[V, T] private[defaultimpl](val parameters: Parameters[V, T]) extends Context[V, T] {

  val interner: IdentityInterner[DefaultDiagram[V, T]] = IdentityInterner.newWeakIdentityInterner()
  val restrictCache: Cache[(DefaultDiagram[V, T], Map[Variable[V], V]), DefaultDiagram[V, T]] = CacheBuilder.newBuilder.maximumSize(parameters.restrictCacheSize).build()
  val applyBinaryOperationCache: Cache[(DefaultDiagram[V, T], (T, T) => T, DefaultDiagram[V, T]), DefaultDiagram[V, T]] = CacheBuilder.newBuilder.maximumSize(parameters.binaryOpCacheSize).build()
  val applyUnaryOperationCache: Cache[(DefaultDiagram[V, T], (T) => T), DefaultDiagram[V, T]] = CacheBuilder.newBuilder.maximumSize(parameters.unaryOpCacheSize).build()

  def getConstantDiagram(value: T): DefaultDiagram[V, T] = interner.intern(new Leaf[V, T](value, this))

  def getSimpleDiagram(variable: Variable[V], children: Map[V, T]): DecisionDiagram[V, T] = {
    require(children.keySet == variable.domain, children + " must exactly contain one mapping for each value of " + variable + ".")
    // mapValues is apparently not a good idea here, because it only returns a view
    getDiagramUnsafe(variable, for ((k, v) <- children) yield k -> getConstantDiagram(v))
  }

  private[defaultimpl] def getDiagramUnsafe(variable: Variable[V], children: Map[V, DefaultDiagram[V, T]]): DefaultDiagram[V, T] = {
    val firstChild: DefaultDiagram[V, T] = children(children.keySet.iterator.next())
    if (children.values.map(_ eq firstChild).reduce(_ && _)) firstChild
    else interner.intern(new InnerNode[V, T](variable, children, this))
  }
}
