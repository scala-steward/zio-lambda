val mainScala = "2.12.10"
val allScala  = Seq(mainScala, "2.13.1")

inThisBuild(
  List(
    organization := "dev.zio",
    organizationName := "ZIO",
    homepage := Some(url("https://github.com/zio/zio-lambda")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalaVersion := mainScala,
    crossScalaVersions := allScala,
    parallelExecution in Test := false,
    fork in Test := true,
    fork in run := true,
    pgpPublicRing := file("/tmp/public.asc"),
    pgpSecretRing := file("/tmp/secret.asc"),
    pgpPassphrase := sys.env.get("PGP_PASSWORD").map(_.toArray),
    scmInfo := Some(
      ScmInfo(url("https://github.com/zio/zio-lambda/"), "scm:git:git@github.com:zio/zio-lambda.git")
    )
  )
)

ThisBuild / publishTo := sonatypePublishToBundle.value

name := "zio-lambda"
scalafmtOnCompile := true

enablePlugins(BuildInfoPlugin)
buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, isSnapshot)
buildInfoPackage := "zio.lambda"
buildInfoObject := "BuildInfo"

libraryDependencies ++= Dependencies.prodDependencies ++ Seq(
  compilerPlugin("org.typelevel" % "kind-projector" % "0.11.0") cross CrossVersion.full
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
