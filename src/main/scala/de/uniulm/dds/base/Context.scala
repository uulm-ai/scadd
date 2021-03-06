package de.uniulm.dds.base

import java.util.concurrent.ConcurrentMap

import com.google.common.collect.MapMaker

/**
  * An interface for decision diagram contexts. The context
  * contains information on variable ordering, takes care of ensuring diagram uniqueness, and ensures that
  * variable names are unique.
  *
  * User: Felix
  * Date: 20.05.13
  * Time: 22:31
  *
  * @tparam V The type of the elements of variable domains
  * @tparam T The type of leaf values of the diagrams
  */
trait Context[V, T] {
  private final val variableNameMap: ConcurrentMap[String, Variable[V]] = new MapMaker().weakValues().makeMap()

  /**
    * Creates a diagram representing the constant mapping to `value`.
    *
    * @param value the constant value
    * @return a diagram representing the constant mapping to `value`.
    */
  def getConstantDiagram(value: T): DecisionDiagram[V, T]

  /**
    * Creates a diagram that maps `variable` to constant values.
    *
    * @param variable the variable the diagram branches on
    * @param children a mapping of elements of the variable's domain values to constant values
    * @return a diagram that maps `variable` to constant values.
    */
  def getSimpleDiagram(variable: Variable[V], children: Map[V, T]): DecisionDiagram[V, T]

  /**
    * Creates a variable.
    *
    * @param name   The name of the variable
    * @param domain The domain of the variable, i.e., the values it can take. Must be a finite set of cardinality of two or more.
    * @return the new variable
    */
  def getVariable(name: String, domain: Set[V]): Variable[V] = {
    val variable = variableNameMap.computeIfAbsent(name, _ => new Variable[V](name, domain))
    require(domain == variable.domain, "Attempting to create variable \"" + name + "\" with domain " + domain + " when a variable with the same name but domain " + variable.domain + " already exists in the context.")
    variable
  }
}

object Context {

  trait ContextFactory[V, T, P[_, _]] {
    def build(p: P[V, T]): Context[V, T]
  }

  /**
    * Creates a new context
    *
    * @param params  the parameters required to create the context
    * @param factory the context factory
    * @tparam V the type of the elements of variable domains
    * @tparam T the type of leaf values of the diagrams
    * @tparam P the type of parameters required to create the context
    * @return the new context
    */
  def apply[V, T, P[_, _]](params: P[V, T])(implicit factory: ContextFactory[V, T, P]): Context[V, T] = factory.build(params)
}
