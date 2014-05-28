package de.uniulm.dds.base

import java.util.Comparator
import org.scalatest.FunSuite

/**
 * Common tests for all context implementation
 *
 * User: felix
 * Date: 03.04.13
 * Time: 09:44
 */
trait AbstractTests extends FunSuite {
  def createContext[V, T](variableOrder: Comparator[Variable[V]]): Context[V, T]

  test("Indicators sum to one") {
    implicit val context: Context[String, Double] = createContext(lexicographicOrdering)
    import ExtraADDImplicits._
    val variable: Variable[String] = Variable("blubb", Set("a", "b", "c"))
    assertResult(DecisionDiagram(1d))(variable.domain.map(x => variable.indicator(x)).reduce(_ + _))
  }

  test("Conversion between Double contexts") {
    val context1: Context[String, Double] = createContext(lexicographicOrdering)
    val context2: Context[String, Double] = createContext(lexicographicOrdering)
    import ExtraADDImplicits._
    val fooIndicator: DecisionDiagram[String, Double] = Variable("foo", Set("a", "b", "c")).indicator("a")(context1, implicitly[Numeric[Double]])
    val barIndicator: DecisionDiagram[String, Double] = Variable("bar", Set("a", "b", "c")).indicator("a")(context1, implicitly[Numeric[Double]])
    val diagram: DecisionDiagram[String, Double] = fooIndicator * barIndicator
    val converted: DecisionDiagram[String, Double] = diagram.convertToContext(context2)
    intercept[IllegalArgumentException] {
      diagram + converted
    }
    intercept[IllegalArgumentException] {
      DecisionDiagram(0d)(context2) * diagram
    }
    assertResult(diagram)(converted.convertToContext(context1))
  }

  test("Inversion, Difference, Negation") {
    implicit val context: Context[String, Double] = createContext(lexicographicOrdering)
    import ExtraADDImplicits._
    val variable: Variable[String] = Variable("blubb", Set("a", "b", "c"))
    val onePlusIndicator: DecisionDiagram[String, Double] = DecisionDiagram(1d) + variable.indicator("a")
    assertResult(onePlusIndicator)(DecisionDiagram(1d) / (DecisionDiagram(1d) / onePlusIndicator))
    assertResult(DecisionDiagram(0d))(onePlusIndicator - onePlusIndicator)
    assertResult(onePlusIndicator)(-(-onePlusIndicator))
  }

  test("Sum out") {
    implicit val context: Context[String, Double] = createContext(lexicographicOrdering)
    import ExtraADDImplicits._
    val variable: Variable[String] = Variable("blubb", Set("a", "b", "c"))
    val onePlusIndicator: DecisionDiagram[String, Double] = DecisionDiagram(1d) + variable.indicator("a")
    assertResult(DecisionDiagram(4d))(onePlusIndicator.opOut(variable, _ + _))
  }

  test("Indicators disjoin to true") {
    implicit val context: Context[String, Boolean] = createContext(lexicographicOrdering)
    import ExtraBDDImplicits._
    val variable: Variable[String] = Variable("blubb", Set("a", "b", "c"))
    assertResult(DecisionDiagram(true))(variable.domain.map(x => variable.indicator(x)).reduce(_ || _))
  }

  test("Conversion between Boolean contexts") {
    val context1: Context[String, Boolean] = createContext(lexicographicOrdering)
    val context2: Context[String, Boolean] = createContext(lexicographicOrdering)
    import ExtraBDDImplicits._
    val variable1: Variable[String] = Variable("foo", Set("a", "b", "c"))
    val variable2: Variable[String] = Variable("bar", Set("a", "b", "c"))
    val diagram: DecisionDiagram[String, Boolean] = variable1.indicator("a")(context1) && variable2.indicator("a")(context1)
    assert(diagram !== diagram.convertToContext(context2))
    assertResult(diagram)(diagram.convertToContext(context2).convertToContext(context1))
  }

  test("DeMorgan") {
    implicit val context: Context[String, Boolean] = createContext(lexicographicOrdering)
    import ExtraBDDImplicits._
    val a: Variable[String] = Variable("a", Set("true", "false"))
    val b: Variable[String] = Variable("b", Set("true", "false"))
    val aTrue: DecisionDiagram[String, Boolean] = a.indicator("true")
    val bTrue: DecisionDiagram[String, Boolean] = b.indicator("true")
    assertResult(aTrue || bTrue)(!(!aTrue && !bTrue))
  }

  test("Get value, i.e., apply()") {
    implicit val context: Context[String, Int] = createContext(lexicographicOrdering)
    import ExtraADDImplicits._
    val variable: Variable[String] = Variable("a", Set("true", "false"))
    val simpleDiagram: DecisionDiagram[String, Int] = variable.indicator("true")
    assertResult(1)(simpleDiagram(Map(variable -> "true")))
    assertResult(0)(simpleDiagram(Map(variable -> "false")))
  }

  test("Interning of constant and simple diagrams") {
    implicit val context: Context[String, Int] = createContext(lexicographicOrdering)
    import ExtraADDImplicits._
    assertResult(DecisionDiagram(1))(DecisionDiagram(1))
    val variable: Variable[String] = Variable("1", Set("true", "false"))
    assertResult(variable.indicator("false"))(variable.indicator("false"))
  }

  test("Creating simple diagrams works with suitable value maps only") {
    implicit val context: Context[String, Int] = createContext(lexicographicOrdering)
    val variable: Variable[String] = Variable("1", Set("true", "false"))
    intercept[IllegalArgumentException] {
      DecisionDiagram(variable, Map("true" -> 0, "false" -> 1, "none" -> 2))
    }
    intercept[IllegalArgumentException] {
      DecisionDiagram(variable, Map("true" -> 0, "none" -> 2))
    }
  }

  test("Restrict a simple diagram") {
    implicit val context: Context[String, Int] = createContext(lexicographicOrdering)
    val variable1: Variable[String] = Variable("1", Set("true", "false"))
    val simpleDiagram: DecisionDiagram[String, Int] = DecisionDiagram(variable1, Map("true" -> 0, "false" -> 1))
    assertResult(DecisionDiagram(0))(simpleDiagram.restrict(Map(variable1 -> "true")))
    assertResult(DecisionDiagram(1))(simpleDiagram.restrict(Map(variable1 -> "false")))
  }

  test("Restrict a non-simple diagram") {
    implicit val context: Context[String, Int] = createContext(lexicographicOrdering)
    import ExtraADDImplicits._
    val variable1: Variable[String] = Variable("1", Set("true", "false"))
    val variable2: Variable[String] = Variable("2", Set("true", "false"))
    val variable1FalseIndicator: DecisionDiagram[String, Int] = variable1.indicator("false")
    val variable1TrueIndicator: DecisionDiagram[String, Int] = variable1.indicator("true")
    val variable2FalseIndicator: DecisionDiagram[String, Int] = variable2.indicator("false")
    val variable2TrueIndicator: DecisionDiagram[String, Int] = variable2.indicator("true")
    val result: DecisionDiagram[String, Int] = variable1TrueIndicator * variable2FalseIndicator + variable1FalseIndicator * variable2TrueIndicator
    assertResult(variable2TrueIndicator)(result.restrict(Map(variable1 -> "false")))
    assertResult(variable2FalseIndicator)(result.restrict(Map(variable1 -> "true")))
    assertResult(variable1TrueIndicator)(result.restrict(Map(variable2 -> "false")))
    assertResult(variable1FalseIndicator)(result.restrict(Map(variable2 -> "true")))
  }

  test("toString() and size()") {
    implicit val context: Context[String, Int] = createContext(lexicographicOrdering)
    import ExtraADDImplicits._
    val variable1: Variable[String] = Variable("a", Set("true", "false"))
    val variable2: Variable[String] = Variable("b", Set("true", "false"))
    val variable1FalseIndicator: DecisionDiagram[String, Int] = variable1.indicator("false")
    val variable1TrueIndicator: DecisionDiagram[String, Int] = variable1.indicator("true")
    val variable2FalseIndicator: DecisionDiagram[String, Int] = variable2.indicator("false")
    val variable2TrueIndicator: DecisionDiagram[String, Int] = variable2.indicator("true")
    val falseBranch: DecisionDiagram[String, Int] = variable1FalseIndicator * variable2TrueIndicator
    val trueBranch: DecisionDiagram[String, Int] = variable1TrueIndicator * variable2FalseIndicator
    val result: DecisionDiagram[String, Int] = trueBranch + falseBranch
    assertResult("(a\n" + "\t(true (b\n" + "\t\t(true (0))\n" + "\t\t(false (1))))\n" + "\t(false (b\n" + "\t\t(true (1))\n" + "\t\t(false (0)))))")(result.toString())
    assertResult(4)((result * Variable("c", Set("true", "false")).indicator("true")).size)
    assertResult(3)(result.size)
    assertResult(0)(DecisionDiagram(5).size)
  }

  test("Compute occurring variables") {
    val variable1: Variable[String] = Variable("a", Set("true", "false"))
    val variable2: Variable[String] = Variable("b", Set("true", "false"))
    implicit val context: Context[String, Int] = createContext(listbasedOrdering(List(variable1, variable2)))
    import ExtraADDImplicits._
    val variable1FalseIndicator: DecisionDiagram[String, Int] = variable1.indicator("false")
    val variable1TrueIndicator: DecisionDiagram[String, Int] = variable1.indicator("true")
    val variable2FalseIndicator: DecisionDiagram[String, Int] = variable2.indicator("false")
    val variable2TrueIndicator: DecisionDiagram[String, Int] = variable2.indicator("true")
    val result: DecisionDiagram[String, Int] = variable1TrueIndicator * variable2FalseIndicator + variable1FalseIndicator * variable2TrueIndicator
    assertResult(Set(variable1, variable2))(result.occurringVariables)
  }
}