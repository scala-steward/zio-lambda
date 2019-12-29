import sbt._

object Dependencies {
  // Dependency versions
  val zioVersion = "1.0.0-RC17"
  val awsVersion = "2.10.41"

  lazy val prodDependencies = Seq(
    // TODO: exclude http clients, use zio-http instead
    "software.amazon.awssdk"       % "cloudwatchlogs"                 % awsVersion,
    "dev.zio"                      %% "zio"                           % zioVersion,
    "dev.zio"                      %% "zio-streams"                   % zioVersion,
    "dev.zio"                      %% "zio-interop-java"              % "1.1.0.0-RC6",
    "dev.zio"                      %% "zio-logging"                   % "0.0.4",
    "com.softwaremill.sttp.client" %% "core"                          % "2.0.0-RC5",
    "com.softwaremill.sttp.client" %% "async-http-client-backend-zio" % "2.0.0-RC5"
  )
}
