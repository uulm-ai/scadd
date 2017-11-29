package de.uniulm.dds.parser

import de.uniulm.dds.base.{Context, DecisionDiagram, Variable}

import scala.util.Try
import scala.util.parsing.combinator.JavaTokenParsers

class ADDParser(implicit context: Context[String, Double]) extends JavaTokenParsers {
  // todo: allow parsing other numerics
  // todo: make sure variables are used consistently, i.e., always with the same set of values
  def leaf: Parser[DecisionDiagram[String, Double]] = "(" ~ floatingPointNumber ~ ")" ^^ { case _ ~ num ~ _ => DecisionDiagram[String, Double](num.toDouble) }

  def valueChildTuple: Parser[(String, DecisionDiagram[String, Double])] = ("(" ~> ident <~ opt(whiteSpace)) ~ (diagram <~ ")" <~ opt(whiteSpace)) ^^ {
    case valueName ~ childDiagram => (valueName, childDiagram)
  }

  def valueMap: Parser[Map[String, DecisionDiagram[String, Double]]] = valueChildTuple ~ rep1(valueChildTuple) ^? {
    case firstTuple ~ restList if (firstTuple +: restList).map(_._1).toSet.size == restList.size + 1 =>
      (firstTuple +: (for ((valueName, childDiagram) <- restList) yield valueName -> childDiagram)).toMap
  }

  def inner: Parser[DecisionDiagram[String, Double]] = "(" ~> (ident <~ opt(whiteSpace)) ~! valueMap <~ ")" ^? {
    case variableName ~ valueMap if Try(Variable(variableName, valueMap.keySet)).isSuccess =>
      val variable = Variable(variableName, valueMap.keySet)
      valueMap.keySet.foldLeft(DecisionDiagram(0d))({ case (current, next) => current + valueMap(next) * variable.indicator(next) })
  }

  def diagram: Parser[DecisionDiagram[String, Double]] = leaf | inner
}
