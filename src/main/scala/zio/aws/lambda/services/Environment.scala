package zio.aws.lambda.services

import java.io.File
import java.time.{ZoneId, ZoneOffset}
import scala.sys.process.Process
import scala.util.Try
import scalaz.zio._
import scalaz.zio.system._
import scalaz.zio.blocking.Blocking
import software.amazon.awssdk.regions.Region
import zio.aws.lambda._

trait Environment extends Serializable {
  def lambdaEnv: Environment.Service[Any]
}

object Environment extends Serializable {
  trait Service[R] extends Serializable {
    def region: Region
    def handler: String
    def taskRoot: File
    def runtimeApi: String
    def runtimeDir: String
    def functionName: String
    def functionMemorySize: Int
    def functionVersion: String
    def logGroupName: String
    def logStreamName: String
    def awsExecutionEnv: String
    def awsAccessKeyId: String
    def awsSecretAccessKey: String
    def awsSessionToken: String
    def lang: String
    def timeZone: ZoneId
    def path: String
    def setTraceId(tid: String): ZIO[R, Throwable, Unit]
  }

  val ENV_HANDLER              = "_HANDLER"
  val ENV_REGION               = "AWS_REGION"
  val ENV_RUNTIME_API          = "AWS_LAMBDA_RUNTIME_API"
  val ENV_RUNTIME_DIR          = "LAMBDA_RUNTIME_DIR"
  val ENV_TASK_ROOT            = "LAMBDA_TASK_ROOT"
  val ENV_FUNCTION_NAME        = "AWS_LAMBDA_FUNCTION_NAME"
  val ENV_FUNCTION_MEMORY_SIZE = "AWS_LAMBDA_FUNCTION_MEMORY_SIZE"
  val ENV_FUNCTION_VERSION     = "AWS_LAMBDA_FUNCTION_VERSION"
  val ENV_LOG_GROUP_NAME       = "AWS_LAMBDA_LOG_GROUP_NAME"
  val ENV_LOG_STREAM_NAME      = "AWS_LAMBDA_LOG_STREAM_NAME"
  val ENV_EXECUTION_ENV        = "AWS_EXECUTION_ENV"
  val ENV_ACCESS_KEY           = "AWS_ACCESS_KEY_ID"
  val ENV_SECRET_ACCESS_KEY    = "AWS_SECRET_ACCESS_KEY"
  val ENV_SESSION_TOKEN        = "AWS_SESSION_TOKEN"
  val ENV_LANG                 = "LANG"
  val ENV_TIMEZONE             = "TZ"
  val ENV_PATH                 = "PATH"

  val all = List(
    ENV_HANDLER,
    ENV_REGION,
    ENV_RUNTIME_API,
    ENV_RUNTIME_DIR,
    ENV_TASK_ROOT,
    ENV_FUNCTION_NAME,
    ENV_FUNCTION_MEMORY_SIZE,
    ENV_FUNCTION_VERSION,
    ENV_LOG_GROUP_NAME,
    ENV_LOG_STREAM_NAME,
    ENV_EXECUTION_ENV,
    ENV_ACCESS_KEY,
    ENV_SECRET_ACCESS_KEY,
    ENV_SESSION_TOKEN,
    ENV_LANG,
    ENV_TIMEZONE,
    ENV_PATH
  )

  def fromEnvironment: ZIO[System, Error, Environment] =
    ZIO
      .foreach(all)(k => env(k).map(k -> _))
      .map(_.toMap)
      .mapError(HttpError(_))
      .flatMap { vars =>
        val missingKeys = vars collect { case (k, None) => k }

        if (missingKeys.nonEmpty)
          IO.fail(MissingEnv(::(missingKeys.head, missingKeys.tail.toList)))
        else
          IO.succeed(new Environment {
            val lambdaEnv = new Service[Any] with Blocking.Live {
              val region             = Try(Region.of(vars(ENV_REGION).get)).getOrElse(Region.EU_WEST_1)
              val handler            = vars(ENV_HANDLER).get
              val taskRoot           = new File(vars(ENV_TASK_ROOT).get)
              val runtimeApi         = vars(ENV_RUNTIME_API).get
              val runtimeDir         = vars(ENV_RUNTIME_DIR).get
              val functionName       = vars(ENV_FUNCTION_NAME).get
              val functionMemorySize = vars(ENV_FUNCTION_MEMORY_SIZE).get.toInt
              val functionVersion    = vars(ENV_FUNCTION_VERSION).get
              val logGroupName       = vars(ENV_LOG_GROUP_NAME).get
              val logStreamName      = vars(ENV_LOG_STREAM_NAME).get
              val awsExecutionEnv    = vars(ENV_EXECUTION_ENV).get
              val awsAccessKeyId     = vars(ENV_ACCESS_KEY).get
              val awsSecretAccessKey = vars(ENV_SECRET_ACCESS_KEY).get
              val awsSessionToken    = vars(ENV_SESSION_TOKEN).get
              val lang               = vars(ENV_LANG).get
              val timeZone           = Try(ZoneId.of(vars(ENV_TIMEZONE).get)).getOrElse(ZoneOffset.UTC)
              val path               = vars(ENV_PATH).get

              def setTraceId(tid: String) = blocking.effectBlocking {
                Process("env", None, "_X_AMZN_TRACE_ID" -> tid).!
              }
            }
          })
      }
      .memoize
      .flatten
}
