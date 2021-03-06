name := "scadd"

organization := "de.uni-ulm"

version := "1.14"

scalaVersion := "2.12.4"

resolvers += "fmueller" at "http://companion.informatik.uni-ulm.de/~fmueller/mvn"

libraryDependencies += "de.uni-ulm" %% "identity_interners" % "2.0"

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"