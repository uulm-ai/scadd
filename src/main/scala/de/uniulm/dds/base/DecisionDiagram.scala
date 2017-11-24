package de.uniulm.dds.base


/**
  * Interface for decision diagrams.
  *
  * User: Felix
  * Date: 20.05.13
  * Time: 14:54
  *
  * @tparam V The type of the elements of variable domains
  * @tparam T The type of leaf values of the diagrams
  */
trait DecisionDiagram[V, T] extends (Map[Variable[V], V] => T) {
  /**
    * The [[de.uniulm.dds.base.Context]] of this DecisionDiagram.
    */
  def context: Context[V, T]

  /**
    * Transforms this decision diagram by recursively applying the <code>operation</code> to leaves and inner nodes.
    * I.e., this method does a post-order depth-first traversal of the decision graph.
    *
    * @param leafTransformation      the operation to apply to leaf values
    * @param innerNodeTransformation the operation to apply to inner nodes, given the variable of the inner node and
    *                                the result of transforming its children
    * @tparam K the result type of the transformation
    * @return the transformation result
    */
  def transform[K](leafTransformation: T => K, innerNodeTransformation: (Variable[V], Map[V, K]) => K): K

  /**
    * Applies a binary operation to this diagram and a given other diagram. Semantically, it is a point-wise
    * application of the given operation to all assignments, i.e.,
    * `result.getValue(x) == operation.apply(this.getValue(x), other.getValue(x))` for all assignments `x`.
    *
    * @param other         the second argument of the operation
    * @param leafOperation the operation to apply
    * @return the result of applying the operation
    */
  def applyBinaryOperation(leafOperation: (T, T) => T, other: DecisionDiagram[V, T]): DecisionDiagram[V, T]

  /**
    * Applies a unary operation to this diagram. Semantically, it is a point-wise application of the given operation
    * to all assignments, i.e., `result.getValue(x) == operation.apply(this.getValue(x))` for all assignments `x`.
    *
    * @param leafOperation the operation to apply
    * @return the result of applying the operation
    */
  def applyUnaryOperation(leafOperation: T => T): DecisionDiagram[V, T]

  /**
    * Implements the restrict operation defined on decision diagrams, i.e., given a partial assignment of variables to
    * values, it returns the diagram that corresponds to restricting all assigned variables to their assigned values.
    *
    * @param assignment a (partial) assignment of variables to values
    * @return the restricted diagram
    */
  def restrict(assignment: Map[Variable[V], V]): DecisionDiagram[V, T]

  /**
    * Removes `variable` from this diagram by restricting, in turn, the variable to all of its possible values and
    * combining the results using `operation`
    *
    * @param variable  the variable to remove
    * @param operation the combination operation
    * @return the resulting diagram
    */
  def opOut(variable: Variable[V], operation: (T, T) => T): DecisionDiagram[V, T] = variable.domain.tail.foldLeft(restrict(Map(variable -> variable.domain.head)))((dd, value) => dd.applyBinaryOperation(operation, restrict(Map(variable -> value))))

  override def toString(): String = {
    def innerNodeTransformation(variable: Variable[V], map: Map[V, String]): String = "(" + variable + map.foldLeft("")((intermediate, tuple) => intermediate + "\n(" + tuple._1 + " " + tuple._2 + ")").replace("\n", "\n\t") + ")"

    transform("(" + _ + ")", innerNodeTransformation)
  }

  /**
    * Returns the set of variables that occur in this diagram
    *
    * @return the set of variables that occur in this diagram
    */
  def occurringVariables: Set[Variable[V]] = {
    val leafTransformation: (T) => Set[Variable[V]] = _ => Set()
    val innerNodeTransformation: (Variable[V], Map[V, Set[Variable[V]]]) => Set[Variable[V]] = (variable, map) => Set(variable) ++ map.values.flatten

    transform(leafTransformation, innerNodeTransformation)
  }

  /**
    * Returns the number of distinct inner nodes that occur in this diagram
    *
    * @return the number of distinct inner nodes that occur in this diagram
    */
  def size: Int

  def +(other: DecisionDiagram[V, T])(implicit n: Numeric[T]): DecisionDiagram[V, T] = applyBinaryOperation(n.plus, other)

  def -(other: DecisionDiagram[V, T])(implicit n: Numeric[T]): DecisionDiagram[V, T] = applyBinaryOperation(n.minus, other)

  def *(other: DecisionDiagram[V, T])(implicit n: Numeric[T]): DecisionDiagram[V, T] = applyBinaryOperation(n.times, other)

  def /(other: DecisionDiagram[V, T])(implicit n: Numeric[T]): DecisionDiagram[V, T] = n match {
    case i: Integral[T] => applyBinaryOperation(i.quot, other)
    case f: Fractional[T] => applyBinaryOperation(f.div, other)
    case _ => sys.error("indivisible numeric!")
  }

  def unary_-(implicit n: Numeric[T]): DecisionDiagram[V, T] = applyUnaryOperation(n.negate)

  def &&(other: DecisionDiagram[V, Boolean])(implicit evidence: DecisionDiagram[V, T] =:= DecisionDiagram[V, Boolean]): DecisionDiagram[V, Boolean] = evidence(this).applyBinaryOperation(_ && _, other)

  def ||(other: DecisionDiagram[V, Boolean])(implicit evidence: DecisionDiagram[V, T] =:= DecisionDiagram[V, Boolean]): DecisionDiagram[V, Boolean] = evidence(this).applyBinaryOperation(_ || _, other)

  def unary_!(implicit evidence: DecisionDiagram[V, T] =:= DecisionDiagram[V, Boolean]): DecisionDiagram[V, Boolean] = evidence(this).applyUnaryOperation(!_)

  /**
    * Converts this diagram into the equivalent diagram of another context.
    *
    * @param targetContext the target context
    * @return the converted diagram
    */
  def convertToContext(targetContext: Context[V, T])(implicit n: Numeric[T]): DecisionDiagram[V, T] = {
    val leafTransformation: (T) => DecisionDiagram[V, T] = x => targetContext.getConstantDiagram(x)
    val innerNodeTransformation: (Variable[V], Map[V, DecisionDiagram[V, T]]) => DecisionDiagram[V, T] =
      (variable, map) => variable.domain.foldLeft(DecisionDiagram(n.fromInt(0))(targetContext))((sum, next) => sum + map(next) * variable.indicator(next)(targetContext, n))

    transform(leafTransformation, innerNodeTransformation)
  }
}

object DecisionDiagram {
  /**
    * Creates a diagram representing the constant mapping to `value`.
    *
    * @param value   the constant value
    * @param context the (possibly implicitly given) context that creates the actual diagram
    * @return a diagram representing the constant mapping to `value`.
    */
  def apply[V, T](value: T)(implicit context: Context[V, T]): DecisionDiagram[V, T] = context.getConstantDiagram(value)

  /**
    * Creates a diagram that maps `variable` to constant values.
    *
    * @param variable the variable the diagram branches on
    * @param children a mapping of elements of the variable's domain values to constant values
    * @param context  the (possibly implicitly given) context that creates the actual diagram
    * @tparam V The type of the elements of variable domains
    * @tparam T The type of leaf values of the diagrams
    * @return a diagram that maps `variable` to constant values.
    */
  def apply[V, T](variable: Variable[V], children: Map[V, T])(implicit context: Context[V, T]): DecisionDiagram[V, T] = context.getSimpleDiagram(variable, children)

  /**
    * Converts a given function into a decision diagram. Caution: this method enumerates all assignments to the given set
    * of variables and therefore requires time exponential in `relevantVariables.size`.
    *
    * @param function          the function to convert
    * @param relevantVariables the variables that need to be assigned for evaluating this function
    * @return a decision diagram representing this function
    */
  def apply[V, T](function: Map[Variable[V], V] => T, relevantVariables: Set[Variable[V]])(implicit context: Context[V, T], n: Numeric[T]): DecisionDiagram[V, T] = {
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
