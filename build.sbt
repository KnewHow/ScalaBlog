scalaVersion := "2.12.10"
name := "scalaBlog"
organization := "ch.epfl.scala"
version := "1.0"
libraryDependencies ++= Seq(
  "org.typelevel"      %% "cats-core"   % "1.1.0",
  "org.scalatest" %% "scalatest" % "3.1.0-RC3" % Test,
  "org.typelevel" %% "cats-effect" % "2.0.0",
  "org.typelevel"      %% "cats-core"   % "2.0.0",
  "com.github.knewhow" %% "scalaprop"   % "1.1.0",
  "org.slf4j"          % "slf4j-api"    % "1.7.5",
  "org.slf4j"          % "slf4j-simple" % "1.7.5"
)
