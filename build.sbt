import de.johoop.jacoco4sbt._
import JacocoPlugin._

name := "scadd"

organization := "de.uni-ulm"

version := "1.10"

scalaVersion := "2.11.0"

resolvers += "fmueller" at "http://companion.informatik.uni-ulm.de/~fmueller/mvn"

libraryDependencies += "com.google.guava" % "guava" % "16.0.1"

libraryDependencies += "com.google.code.findbugs" % "jsr305" % "1.3.9"

libraryDependencies += "de.uni-ulm" %% "scala_util" % "1.3"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.7" % "test"

jacoco.settings
