import sbt._
import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "io.github.regiskuckaertz"
ThisBuild / organizationName := "Regis Kuckaertz"

lazy val root = (project in file("."))
  .settings(
    name := "zio-lambda-runtime",
    libraryDependencies ++= prodDependencies ++ Seq(
      compilerPlugin("org.typelevel" %% "kind-projector" % "0.10.1")
    ),
    scalacOptions += "-Ypartial-unification"
  )

