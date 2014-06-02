package de.uniulm.dds


/**
 * Holds implicit classes for providing additional methods on number-valued decision diagrams (i.e., ADDs) and
 * boolean-valued decision diagrams (i.e., BDDs).
 *
 * User: Felix
 * Date: 27.05.13
 * Time: 19:55
 */
package object base {

  /**
   * Creates a variable ordering for cases when variable order is given as an ordered list of variables.
   *
   * @tparam V The type of the elements of variable domains
   * @param list the list whose indices define the order of the contained variables. The comparison result for
   *             variables <code>o1</code> and <code>o2</code> is <code>list.indexOf(o1) - list.indexOf(o2)</code>.
   */
  def listbasedOrdering[V](list: Seq[Variable[V]]): Ordering[Variable[V]] = new Ordering[Variable[V]] {
    override def compare(x: Variable[V], y: Variable[V]): Int = {
      val i1: Int = list.indexOf(x)
      require(i1 != -1, "First argument " + x + " is not contained in the defining list.")
      val i2: Int = list.indexOf(y)
      require(i2 != -1, "Second argument " + y + " is not contained in the defining list.")
      i1 - i2
    }
  }

  /**
   * Creates a variable ordering based on the lexicographic ordering of the variable names
   *
   * @tparam V The type of the elements of variable domains
   * @return the lexicographic ordering
   */
  def lexicographicOrdering[V]: Ordering[Variable[V]] = new Ordering[Variable[V]] {
    override def compare(x: Variable[V], y: Variable[V]): Int = x.name.compareTo(y.name)
  }

  /**
   * Extra implicits for usage with ADDs
   */
  object ExtraADDImplicits {

    implicit class RichVariable[V](variable: Variable[V]) {
      /**
       * Creates an indicator diagram that map `indicatedValue` to 1 and all other values to 0.
       * @param indicatedValue the indicated value
       * @param context the context to use for constructing the indicator diagram
       * @param n the numeric
       * @tparam T the number type
       * @return the indicator diagram
       */
      def indicator[T](indicatedValue: V)(implicit context: Context[V, T], n: Numeric[T]): DecisionDiagram[V, T] =
        DecisionDiagram(variable, variable.domain.map[(V, T), Map[V, T]](x => if (x == indicatedValue) x -> n.one else x -> n.zero)(collection.breakOut))
    }

    implicit class ADD[V, T](diagram: DecisionDiagram[V, T])(implicit n: Numeric[T]) {

      /**
       * Converts this diagram into the equivalent diagram of another context.
       * @param targetContext the target context
       * @return the converted diagram
       */
      def convertToContext(targetContext: Context[V, T]): DecisionDiagram[V, T] = {
        val leafTransformation: (T) => DecisionDiagram[V, T] = x => targetContext.getConstantDiagram(x)
        val innerNodeTransformation: (Variable[V], Map[V, DecisionDiagram[V, T]]) => DecisionDiagram[V, T] =
          (variable, map) => variable.domain.foldLeft(DecisionDiagram(n.fromInt(0))(targetContext))((sum, next) => sum + map(next) * variable.indicator(next)(targetContext, n))

        diagram.transform(leafTransformation, innerNodeTransformation)
      }
    }

    implicit class RichFunction[V, T](function: Map[Variable[V], V] => T)(implicit context: Context[V, T], n: Numeric[T]) {

      /**
       * Converts this function into a decision diagram
       * @param relevantVariables the variables that need to be assigned for evaluating this function
       * @return a decision diagram representing this function
       */
      def toDecisionDiagram(relevantVariables: Set[Variable[V]]): DecisionDiagram[V, T] = {
        def build(partialAssignment: Map[Variable[V], V], remainingVariables: Set[Variable[V]]): DecisionDiagram[V, T] = {
          if (remainingVariables.isEmpty) DecisionDiagram(function(partialAssignment))
          else {
            val nextVariable = remainingVariables.head
            nextVariable.domain.foldLeft(DecisionDiagram(n.zero))((diagram, value) => diagram + nextVariable.indicator(value) * build(partialAssignment + (nextVariable -> value), remainingVariables.tail))
          }
        }
        build(Map(), relevantVariables)
      }
    }
  }

  /**
   * Extra implicits for usage with BDDs
   */
  object ExtraBDDImplicits {

    implicit class RichVariable[V](variable: Variable[V]) {
      /**
       * Creates an indicator diagram that map `indicatedValue` to `true` and all other values to `false`.
       * @param indicatedValue the indicated value
       * @param context the context to use for constructing the indicator diagram
       * @return the indicator diagram
       */
      def indicator(indicatedValue: V)(implicit context: Context[V, Boolean]): DecisionDiagram[V, Boolean] =
        DecisionDiagram(variable, variable.domain.map[(V, Boolean), Map[V, Boolean]](x => x -> (x == indicatedValue))(collection.breakOut))
    }

    implicit class BDD[V](diagram: DecisionDiagram[V, Boolean]) {
      /**
       * Converts this diagram into the equivalent diagram of another context.
       * @param targetContext the target context
       * @return the converted diagram
       */
      def convertToContext(targetContext: Context[V, Boolean]): DecisionDiagram[V, Boolean] = {
        val leafTransformation: (Boolean) => DecisionDiagram[V, Boolean] = targetContext.getConstantDiagram
        val innerNodeTransformation: (Variable[V], Map[V, DecisionDiagram[V, Boolean]]) => DecisionDiagram[V, Boolean] =
          (variable, map) => variable.domain.foldLeft(DecisionDiagram(false)(targetContext))((sum, next) => sum || map(next) && variable.indicator(next)(targetContext))

        diagram.transform(leafTransformation, innerNodeTransformation)
      }
    }


    implicit class RichFunction[V](function: Map[Variable[V], V] => Boolean)(implicit context: Context[V, Boolean]) {

      /**
       * Converts this function into a decision diagram
       * @param relevantVariables the variables that need to be assigned for evaluating this function
       * @return a decision diagram representing this function
       */
      def toDecisionDiagram(relevantVariables: Set[Variable[V]]): DecisionDiagram[V, Boolean] = {
        def build(partialAssignment: Map[Variable[V], V], remainingVariables: Set[Variable[V]]): DecisionDiagram[V, Boolean] = {
          if (remainingVariables.isEmpty) DecisionDiagram(function(partialAssignment))
          else {
            val nextVariable = remainingVariables.head
            nextVariable.domain.foldLeft(DecisionDiagram(false))((diagram, value) => diagram || nextVariable.indicator(value) && build(partialAssignment + (nextVariable -> value), remainingVariables.tail))
          }
        }
        build(Map(), relevantVariables)
      }
    }
  }
}
