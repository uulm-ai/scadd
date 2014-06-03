name := "scadd"

organization := "de.uni-ulm"

version := "1.12"

scalaVersion := "2.11.0"

resolvers += "fmueller" at "http://companion.informatik.uni-ulm.de/~fmueller/mvn"

libraryDependencies += "de.uni-ulm" %% "identity_interners" % "1.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.7" % "test"