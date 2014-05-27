package de.uniulm.dds.base

/**
 * An interface for decision diagram contexts. The context
 * contains information on variable ordering, takes care of ensuring diagram uniqueness, and so on.
 *
 * User: Felix
 * Date: 20.05.13
 * Time: 22:31
 * @tparam V The type of the elements of variable domains
 * @tparam T The type of leaf values of the diagrams
 */
trait Context[V, T] {
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
}
