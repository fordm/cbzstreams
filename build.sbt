lazy val scala212 = "2.12.17"
lazy val scala213 = "2.13.10"
lazy val scala3 = "3.2.2"
lazy val supportedScalaVersions = List(scala213, scala3)

ThisBuild / organization := "name.michaelford"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := scala213

val catsVersion = "2.8.0"
val scalaTestVersion = "3.2.14"
val zioVersion = "2.0.10"

/*lazy val testDeps = Seq(
  "org.scalatest"   %% "scalatest"  % scalaTestVersion % Test,
  "org.scalatest" %% "scalatest-freespec" % scalaTestVersion % Test,
  "org.scalatest" %% "scalatest-propspec" % scalaTestVersion % Test,
  "org.scalatestplus" %% "scalacheck-1-16" % "3.2.14.0" % Test,
)*/

lazy val zioDeps = Seq(
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-streams" % zioVersion,
  "dev.zio" %% "zio-test" % zioVersion,
  "dev.zio" %% "zio-test-sbt" % zioVersion
)

libraryDependencies in Global ++= Seq(
  "org.typelevel" %% "cats-core" % "2.9.0",
  "ch.qos.logback" % "logback-classic" % "1.2.10" % Runtime,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
)

lazy val root = project.in(file("."))
  .aggregate(cbapi, zstreamapp)
  .settings(
    name := "cbzstream",
    crossScalaVersions := Nil,
    publish / skip := false
  )

lazy val cbapi = project.in(file("cbapi"))
  .settings(
    crossScalaVersions := supportedScalaVersions
  )

lazy val zstreamapp = project.in(file("zstreamapp"))
  .dependsOn(cbapi)
  .settings(
    crossScalaVersions := supportedScalaVersions
  )
  .settings(libraryDependencies ++=
    zioDeps
  )

