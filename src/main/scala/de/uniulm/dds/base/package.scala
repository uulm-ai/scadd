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
   * Extra implicits for usage with BDDs
   */
  object ExtraBDDImplicits {

    /**
     * Implements a Numeric for Booleans via interpreting true as 1, false as 0, and calculating modulo 2
     */
    implicit object BooleanNumeric extends Integral[Boolean] {
      override def plus(x: Boolean, y: Boolean): Boolean = x ^ y

      override def toDouble(x: Boolean): Double = toInt(x)

      override def toFloat(x: Boolean): Float = toInt(x)

      override def toInt(x: Boolean): Int = if (x) 1 else 0

      override def negate(x: Boolean): Boolean = x

      override def fromInt(x: Int): Boolean = x % 2 != 0

      override def toLong(x: Boolean): Long = toInt(x)

      override def times(x: Boolean, y: Boolean): Boolean = x && y

      override def minus(x: Boolean, y: Boolean): Boolean = x ^ y

      override def compare(x: Boolean, y: Boolean): Int = Integer.compare(toInt(x), toInt(y))

      override def quot(x: Boolean, y: Boolean): Boolean = fromInt(toInt(x) / toInt(y))

      override def rem(x: Boolean, y: Boolean): Boolean = fromInt(toInt(x) % toInt(y))
    }

  }

}
