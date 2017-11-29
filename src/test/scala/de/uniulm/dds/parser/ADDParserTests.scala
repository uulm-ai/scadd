package de.uniulm.dds.parser

import de.uniulm.dds.base._
import de.uniulm.dds.leanimpl.Parameters
import org.scalatest.{FunSuite, Matchers}

class ADDParserTests extends FunSuite with Matchers {
  implicit val context: Context[String, Double] = Context(Parameters[String, Double](lexicographicOrdering))

  val constantDiagrams = Map(
    "(1.0)" -> DecisionDiagram(1d),
    "(1)" -> DecisionDiagram(1d),
    "(.1)" -> DecisionDiagram(.1d),
    "(0)" -> DecisionDiagram(0d),
    "(.0)" -> DecisionDiagram(0d),
    "(1f)" -> DecisionDiagram(1d),
    "(1e1)" -> DecisionDiagram(10d),
    "(0d)" -> DecisionDiagram(0d))

  val aXorB: DecisionDiagram[String, Double] = {
    val variable1: Variable[String] = Variable("a", Set("true", "false"))
    val variable2: Variable[String] = Variable("b", Set("true", "false"))
    val variable1FalseIndicator: DecisionDiagram[String, Double] = variable1.indicator("false")
    val variable1TrueIndicator: DecisionDiagram[String, Double] = variable1.indicator("true")
    val variable2FalseIndicator: DecisionDiagram[String, Double] = variable2.indicator("false")
    val variable2TrueIndicator: DecisionDiagram[String, Double] = variable2.indicator("true")
    val falseBranch: DecisionDiagram[String, Double] = variable1FalseIndicator * variable2TrueIndicator
    val trueBranch: DecisionDiagram[String, Double] = variable1TrueIndicator * variable2FalseIndicator
    val result: DecisionDiagram[String, Double] = trueBranch + falseBranch
    result
  }

  val complexDiagrams = Map(
    "(a\n\t(true (1))\n\t(false (0)))" -> Variable("a", Set("false", "true")).indicator("true"),
    "(a\n\t(true (b\n\t\t(true (0))\n\t\t(false (1))))\n\t(false (b\n\t\t(true (1))\n\t\t(false (0)))))" -> aXorB
  )

  val allValidDiagrams: Map[String, DecisionDiagram[String, Double]] = constantDiagrams ++ complexDiagrams

  val invalidDiagrams: Set[String] = Set(
    // value 'true' is used twice
    "(a\n\t(true (0))\n\t(true (1)))",
    // variable 'b' is once used with 2 and once with 3 values in its domain
    "(a\n\t(true (b\n\t\t(true (0))\n\t\t(false (1))))\n\t(false (b\n\t\t(true (1))\n\t\t(false (0))\n\t\t(nope (0)))))"
  )

  val parser = new ADDParser()

  test("Parser correctly parses constant diagrams") {
    constantDiagrams.foreach({ case (input, expected) =>
      val parseResult = parser.parse(parser.leaf, input)
      parseResult should matchPattern { case parser.Success(_, _) => }
      assertResult(expected)(parseResult.get)
    })
  }

  test("Parser correctly parses complex diagrams") {
    complexDiagrams.foreach({ case (input, expected) =>
      val parseResult = parser.parse(parser.diagram, input)
      parseResult should matchPattern { case parser.Success(_, _) => }
      assertResult(expected)(parseResult.get)
    })
  }

  test("The Strings returned by toString can be parsed back") {
    allValidDiagrams.values.toSet.foreach((diagram: DecisionDiagram[String, Double]) => {
      val parseResult = parser.parse(parser.diagram, diagram.toString())
      parseResult should matchPattern { case parser.Success(_, _) => }
      assertResult(diagram)(parseResult.get)
    })
  }

  test("Attempting to parse an invalid diagrams fails") {
    invalidDiagrams.foreach(string => {
      val parseResult = parser.parse(parser.diagram, string)
      parseResult should matchPattern { case parser.Error(_, _) => }
    })
  }
}
