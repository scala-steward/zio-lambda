package zio.aws

import com.softwaremill.sttp.{Response, Uri}
import java.time.ZoneId
import scalaz.zio.ZIO
import scalaz.zio.system.System
import software.amazon.awssdk.regions.Region
import zio.services._

package object lambda {
  type Runtime = Environment with Http

  type UIOR[-R, +A] = ZIO[R, Nothing, A]

  val region: UIOR[Runtime, Region]                    = ZIO.access(_.lambdaEnv.region)
  val runtimeApi: UIOR[Runtime, String]                = ZIO.access(_.lambdaEnv.runtimeApi)
  val runtimeDir: UIOR[Runtime, String]                = ZIO.access(_.lambdaEnv.runtimeDir)
  val functionName: UIOR[Runtime, String]              = ZIO.access(_.lambdaEnv.functionName)
  val functionMemorySize: UIOR[Runtime, Int]           = ZIO.access(_.lambdaEnv.functionMemorySize)
  val functionVersion: UIOR[Runtime, String]           = ZIO.access(_.lambdaEnv.functionVersion)
  val logGroupName: UIOR[Runtime, String]              = ZIO.access(_.lambdaEnv.logGroupName)
  val logStreamName: UIOR[Runtime, String]             = ZIO.access(_.lambdaEnv.logStreamName)
  val awsExecutionEnv: UIOR[Runtime, String]           = ZIO.access(_.lambdaEnv.awsExecutionEnv)
  val awsAccessKeyId: UIOR[Runtime, String]            = ZIO.access(_.lambdaEnv.awsAccessKeyId)
  val awsSecretAccessKey: UIOR[Runtime, String]        = ZIO.access(_.lambdaEnv.awsSecretAccessKey)
  val awsSessionToken: UIOR[Runtime, String]           = ZIO.access(_.lambdaEnv.awsSessionToken)
  val lang: UIOR[Runtime, String]                      = ZIO.access(_.lambdaEnv.lang)
  val timeZone: UIOR[Runtime, ZoneId]                  = ZIO.access(_.lambdaEnv.timeZone)
  val path: UIOR[Runtime, String]                      = ZIO.access(_.lambdaEnv.path)
  def setTraceId(traceId: String): UIOR[Runtime, Unit] = ZIO.access(_.lambdaEnv.setTraceId(traceId))

  def httpGet(
      uri: Uri,
      headers: Map[String, String] = Map.empty
  ): ZIO[Runtime, Error, Response[String]] =
    ZIO.accessM(_.http.get(uri, headers))

  def httpPost(
      uri: Uri,
      body: String,
      headers: Map[String, String] = Map.empty
  ): ZIO[Runtime, Error, Response[String]] =
    ZIO.accessM(_.http.post(uri, body, headers))
    
  def curl(verb: String, uri: Uri, args: String*): ZIO[Runtime, Error, Unit] =
    ZIO.accessM(_.http.curl(verb, uri, args: _*))
}
