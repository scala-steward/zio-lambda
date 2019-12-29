package zio.aws.lambda.environment

import java.time.{ ZoneId, ZoneOffset }
import scala.sys.process.Process
import software.amazon.awssdk.regions.Region
import zio._
import zio.system._
import zio.blocking.Blocking

trait Environment extends Serializable {
  def environment: Environment.Service[Any]
}

object Environment extends Serializable {
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

  trait Service[R] extends Serializable {
    def handler: URIO[R, String]
    def region: ZIO[R, Nothing, Region]
    def runtimeApi: URIO[R, String]
    def runtimeDir: URIO[R, String]
    def taskRoot: URIO[R, String]
    def functionName: URIO[R, String]
    def functionMemorySize: URIO[R, String]
    def functionVersion: URIO[R, String]
    def logGroupName: URIO[R, String]
    def logStreamName: URIO[R, String]
    def awsExecutionEnv: URIO[R, String]
    def awsAccessKeyId: URIO[R, String]
    def awsSecretAccessKey: URIO[R, String]
    def awsSessionToken: URIO[R, String]
    def lang: URIO[R, String]
    def timeZone: URIO[R, ZoneId]
    def path: URIO[R, String]
    def setTraceId(tid: String): URIO[R, Unit]
  }

  trait SystemEnvironment extends Environment {
    protected def system: System.Service[Any]

    protected def blocking: Blocking.Service[Any]

    private[this] def get(key: String): UIO[String] = system.env(key).map(_.getOrElse("")).orDie.memoize.flatten

    final val handler: UIO[String] = get(ENV_HANDLER)
    final val region: UIO[Region] = system
      .env(ENV_REGION)
      .map(_.fold(Region.AWS_GLOBAL)(Region.of(_)))
      .orDie
      .memoize
      .flatten

    final val runtimeApi: UIO[String]         = get(ENV_RUNTIME_API)
    final val runtimeDir: UIO[String]         = get(ENV_RUNTIME_DIR)
    final val taskRoot: UIO[String]           = get(ENV_TASK_ROOT)
    final val functionName: UIO[String]       = get(ENV_FUNCTION_NAME)
    final val functionMemorySize: UIO[String] = get(ENV_FUNCTION_MEMORY_SIZE)
    final val functionVersion: UIO[String]    = get(ENV_FUNCTION_VERSION)
    final val logGroupName: UIO[String]       = get(ENV_LOG_GROUP_NAME)
    final val logStreamName: UIO[String]      = get(ENV_LOG_STREAM_NAME)
    final val awsExecutionEnv: UIO[String]    = get(ENV_EXECUTION_ENV)
    final val awsAccessKeyId: UIO[String]     = get(ENV_ACCESS_KEY)
    final val awsSecretAccessKey: UIO[String] = get(ENV_SECRET_ACCESS_KEY)
    final val awsSessionToken: UIO[String]    = get(ENV_SESSION_TOKEN)
    final val lang: UIO[String]               = get(ENV_LANG)
    final val timeZone: UIO[ZoneId] = system
      .env(ENV_TIMEZONE)
      .map(_.fold[ZoneId](ZoneOffset.UTC)(ZoneId.of(_)))
      .orDie
      .memoize
      .flatten
    final val path: UIO[String] = get(ENV_PATH)
    final def setTraceId(tid: String): UIO[Unit] =
      blocking.effectBlocking(Process("env" :: s"_X_AMZN_TRACE_ID=$tid" :: Nil).!).ignore
  }

  object Live extends SystemEnvironment {
    val system   = System.Live.system
    val blocking = Blocking.Live.blocking
  }
}
