package de.uniulm.dds.parser

import de.uniulm.dds.base.{Context, DecisionDiagram, Variable}

import scala.util.parsing.combinator.JavaTokenParsers

class ADDParser(implicit context: Context[String, Double]) extends JavaTokenParsers {
  // todo: allow parsing other numerics
  // todo: make sure variables are used consistently, i.e., always with the same set of values
  def leaf: Parser[DecisionDiagram[String, Double]] = "(" ~ floatingPointNumber ~ ")" ^^ { case _ ~ num ~ _ => DecisionDiagram[String, Double](num.toDouble) }

  def inner: Parser[DecisionDiagram[String, Double]] = "(" ~ ident ~ opt(whiteSpace) ~ rep1("(" ~ ident ~ opt(whiteSpace) ~ diagram ~ ")" ~ opt(whiteSpace)) ~ ")" ^^ {
    case _ ~ variableName ~ _ ~ ifThenList ~ _ =>
      val valueMap: Map[String, DecisionDiagram[String, Double]] = (for ((_ ~ valueName ~ _ ~ childDiagram ~ _ ~ _) <- ifThenList) yield valueName -> childDiagram).toMap
      require(valueMap.size == ifThenList.size)
      val variable = Variable(variableName, valueMap.keySet)
      valueMap.keySet.foldLeft(DecisionDiagram(0d))({case (current, next) => current + valueMap(next) * variable.indicator(next)})
  }

  def diagram: Parser[DecisionDiagram[String, Double]] = leaf | inner
}
