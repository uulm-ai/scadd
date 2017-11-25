package de.uniulm.dds.parser

import de.uniulm.dds.base.{Context, DecisionDiagram}

import scala.util.parsing.combinator.JavaTokenParsers

class ADDParser(implicit context: Context[String, Double]) extends JavaTokenParsers {
  def leaf: Parser[DecisionDiagram[String, Double]] = "(" ~ floatingPointNumber ~ ")" ^^ { case _ ~ num ~ _ => DecisionDiagram[String, Double](num.toDouble) }
}
