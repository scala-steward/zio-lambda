package zio.aws.lambda.environment

import java.time.{ZoneId, ZoneOffset}
import scala.sys.process.Process
import software.amazon.awssdk.regions.Region
import zio._
import zio.system._
import zio.aws.lambda._

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
    def handler: ZIO[R, Error, String]
    def region: ZIO[R, Error, Region]
    def runtimeApi: ZIO[R, Error, String]
    def runtimeDir: ZIO[R, Error, String]
    def taskRoot: ZIO[R, Error, String]
    def functionName: ZIO[R, Error, String]
    def functionMemorySize: ZIO[R, Error, String]
    def functionVersion: ZIO[R, Error, String]
    def logGroupName: ZIO[R, Error, String]
    def logStreamName: ZIO[R, Error, String]
    def awsExecutionEnv: ZIO[R, Error, String]
    def awsAccessKeyId: ZIO[R, Error, String]
    def awsSecretAccessKey: ZIO[R, Error, String]
    def awsSessionToken: ZIO[R, Error, String]
    def lang: ZIO[R, Error, String]
    def timeZone: ZIO[R, Error, String]
    def path: ZIO[R, Error, String]
    def setTraceId(tid: String): ZIO[R, Error, Unit]
  }

  trait SystemEnvironment extends Environment {
    protected def system: System.Service[Any]

    private[this] def get(key: String): IO[Error, String] =
      system.env(key).foldM(
        _ => IO.fail(AccessDenied),
        {
          case None => IO.fail(MissingVariable)
          case Some(otherwise) => IO.succeed(otherwise)
        }
      ).memoize.flatten

    final val handler: IO[Error, String] = get(ENV_HANDLER)
    final val region: IO[Error, Region] = get(ENV_REGION).flatMap { region =>
      IO.effect(Region.of(region)).catchAll(x => InvalidValue(region, x))
    }
    final val runtimeApi: IO[Error, String] = get(ENV_RUNTIME_API)
    final val runtimeDir: IO[Error, String] = get(ENV_RUNTIME_DIR)
    final val taskRoot: IO[Error, String] = get(ENV_TASK_ROOT)
    final val functionName: IO[Error, String] = get(ENV_FUNCTION_NAME)
    final val functionMemorySize: IO[Error, String] = get(ENV_FUNCTION_MEMORY_SIZE)
    final val functionVersion: IO[Error, String] = get(ENV_FUNCTION_VERSION)
    final val logGroupName: IO[Error, String] = get(ENV_LOG_GROUP_NAME)
    final val logStreamName: IO[Error, String] = get(ENV_LOG_STREAM_NAME)
    final val awsExecutionEnv: IO[Error, String] = get(ENV_EXECUTION_ENV)
    final val awsAccessKeyId: IO[Error, String] = get(ENV_ACCESS_KEY)
    final val awsSecretAccessKey: IO[Error, String] = get(ENV_SECRET_ACCESS_KEY)
    final val awsSessionToken: IO[Error, String] = get(ENV_SESSION_TOKEN)
    final val lang: IO[Error, String] = get(ENV_LANG)
    final val timeZone: IO[Error, String] = get(ENV_TIMEZONE)
    final val path: IO[Error, String] = get(ENV_PATH)
    final def setTraceId(tid: String): IO[Error, Unit] = ???
  }

  sealed trait Error extends Product with Serializable
  final case class InvalidValue(value: String, underlying: Throwable) extends Error
  case object AccessDenied extends Error
  case object MissingVariable extends Error
}
