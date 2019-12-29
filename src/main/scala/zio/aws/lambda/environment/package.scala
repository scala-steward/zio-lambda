package zio.aws.lambda

import java.time.ZoneId
import software.amazon.awssdk.regions.Region
import zio.{ URIO, ZIO }

package object environment {
  def handler: URIO[Environment, String]               = ZIO.accessM(_.environment.handler)
  def region: URIO[Environment, Region]                = ZIO.accessM(_.environment.region)
  def runtimeApi: URIO[Environment, String]            = ZIO.accessM(_.environment.runtimeApi)
  def runtimeDir: URIO[Environment, String]            = ZIO.accessM(_.environment.runtimeDir)
  def taskRoot: URIO[Environment, String]              = ZIO.accessM(_.environment.taskRoot)
  def functionName: URIO[Environment, String]          = ZIO.accessM(_.environment.functionName)
  def functionMemorySize: URIO[Environment, String]    = ZIO.accessM(_.environment.functionMemorySize)
  def functionVersion: URIO[Environment, String]       = ZIO.accessM(_.environment.functionVersion)
  def logGroupName: URIO[Environment, String]          = ZIO.accessM(_.environment.logGroupName)
  def logStreamName: URIO[Environment, String]         = ZIO.accessM(_.environment.logStreamName)
  def awsExecutionEnv: URIO[Environment, String]       = ZIO.accessM(_.environment.awsExecutionEnv)
  def awsAccessKeyId: URIO[Environment, String]        = ZIO.accessM(_.environment.awsAccessKeyId)
  def awsSecretAccessKey: URIO[Environment, String]    = ZIO.accessM(_.environment.awsSecretAccessKey)
  def awsSessionToken: URIO[Environment, String]       = ZIO.accessM(_.environment.awsSessionToken)
  def lang: URIO[Environment, String]                  = ZIO.accessM(_.environment.lang)
  def timeZone: URIO[Environment, ZoneId]              = ZIO.accessM(_.environment.timeZone)
  def path: URIO[Environment, String]                  = ZIO.accessM(_.environment.path)
  def setTraceId(tid: String): URIO[Environment, Unit] = ZIO.accessM(_.environment.setTraceId(tid))
}
