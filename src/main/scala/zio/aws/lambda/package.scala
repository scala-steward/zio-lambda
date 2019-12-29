package zio.aws

import sttp.client.{ Response => SResponse }
import sttp.model.Uri
import java.time.ZoneId
import zio.ZIO
import zio.system.System
import software.amazon.awssdk.regions.Region
import zio.aws.lambda.services._

package object lambda {
  type UIOR[-R, +A] = ZIO[R, Nothing, A]

  val region: UIOR[Runtime, Region]                    = ZIO.access(_.env.region)
  val runtimeApi: UIOR[Runtime, String]                = ZIO.access(_.env.runtimeApi)
  val runtimeDir: UIOR[Runtime, String]                = ZIO.access(_.env.runtimeDir)
  val functionName: UIOR[Runtime, String]              = ZIO.access(_.env.functionName)
  val functionMemorySize: UIOR[Runtime, Int]           = ZIO.access(_.env.functionMemorySize)
  val functionVersion: UIOR[Runtime, String]           = ZIO.access(_.env.functionVersion)
  val logGroupName: UIOR[Runtime, String]              = ZIO.access(_.env.logGroupName)
  val logStreamName: UIOR[Runtime, String]             = ZIO.access(_.env.logStreamName)
  val awsExecutionEnv: UIOR[Runtime, String]           = ZIO.access(_.env.awsExecutionEnv)
  val awsAccessKeyId: UIOR[Runtime, String]            = ZIO.access(_.env.awsAccessKeyId)
  val awsSecretAccessKey: UIOR[Runtime, String]        = ZIO.access(_.env.awsSecretAccessKey)
  val awsSessionToken: UIOR[Runtime, String]           = ZIO.access(_.env.awsSessionToken)
  val lang: UIOR[Runtime, String]                      = ZIO.access(_.env.lang)
  val timeZone: UIOR[Runtime, ZoneId]                  = ZIO.access(_.env.timeZone)
  val path: UIOR[Runtime, String]                      = ZIO.access(_.env.path)
  def setTraceId(traceId: String): UIOR[Runtime, Unit] = ZIO.access(_.env.setTraceId(traceId))

  def httpGet(
    uri: Uri,
    headers: Map[String, String] = Map.empty
  ): ZIO[Runtime, Error, SResponse[String]] =
    ZIO.accessM(_.http.get(uri, headers))

  def httpPost(
    uri: Uri,
    body: String,
    headers: Map[String, String] = Map.empty
  ): ZIO[Runtime, Error, SResponse[String]] =
    ZIO.accessM(_.http.post(uri, body, headers))

  def curl(verb: String, uri: Uri, args: String*): ZIO[Runtime, Error, Unit] =
    ZIO.accessM(_.http.curl(verb, uri, args: _*))
}
