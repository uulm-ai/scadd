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

  val parser = new ADDParser()

  test("Parser correctly parses constant diagrams") {
    constantDiagrams.foreach({ case (input, expected) =>
      val parseResult = parser.parse(parser.leaf, input)
      parseResult should matchPattern { case parser.Success(_, _) => }
      assertResult(expected)(parseResult.get)
    })
  }

  test("The Strings returned by toString can be parsed back") {
    constantDiagrams.values.foreach(diagram => {
      val parseResult = parser.parse(parser.leaf, diagram.toString())
      parseResult should matchPattern { case parser.Success(_, _) => }
      assertResult(diagram)(parseResult.get)
    })
  }
}
