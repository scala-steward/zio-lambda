import sbt._
import Dependencies._

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "dev.zio"
ThisBuild / organizationName := "ZIO"

lazy val root = (project in file("."))
  .settings(
    name := "zio-lambda",
    libraryDependencies ++= prodDependencies ++ Seq(
      compilerPlugin("org.typelevel" % "kind-projector" % "0.11.0") cross CrossVersion.full
    )
  )

