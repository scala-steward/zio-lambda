import sbt._

object Dependencies {
  // Dependency versions
  val zioVersion  = "1.0-RC5"
  val sttpVersion = "1.5.17"
  val awsVersion  = "2.5.54"

  lazy val prodDependencies = Seq(
    "org.scalaz"             %% "scalaz-zio"                    % zioVersion,
    "org.scalaz"             %% "scalaz-zio-streams"            % zioVersion,
    "com.softwaremill.sttp"  %% "core"                          % sttpVersion,
    "com.softwaremill.sttp"  %% "async-http-client-backend-zio" % sttpVersion,
    "software.amazon.awssdk" % "cloudwatch"                     % awsVersion
  )
}
