package zio.logging.cloudwatch

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsAsyncClient
import software.amazon.awssdk.services.cloudwatchlogs.model.{ InputLogEvent, PutLogEventsRequest }
import software.amazon.awssdk.auth.credentials.{ AwsSessionCredentials, StaticCredentialsProvider }
import zio.{ Cause, Task, UIO, ZIO }
import zio.clock.Clock
import zio.interop.javaz
import zio.logging.Logging
import zio.aws.lambda.environment._
import java.time.OffsetDateTime

trait CloudWatchLogger extends Logging.Service[Any, String] {
  protected val client: CloudWatchLogsAsyncClient
  protected val logGroupName: String
  protected val logStreamName: String

  // TODO Stream logs to a queue, create a fiber to poll the queue and accumulate
  // logs up to a PutLogEventsRequest limits and send the request
  private[this] def log(msg: String, time: OffsetDateTime): UIO[Unit] =
    javaz
      .fromCompletionStage(UIO {
        val request = PutLogEventsRequest
          .builder()
          .logGroupName(logGroupName)
          .logStreamName(logStreamName)
          .logEvents(InputLogEvent.builder().message(msg).timestamp(time.toInstant.toEpochMilli).build)
          .build
        client.putLogEvents(request)
      })
      .catchAll(_ => UIO.unit)
      .unit

  def clock: Clock.Service[Any]

  final def trace(message: => String): UIO[Unit] =
    clock.currentDateTime >>= (log("[TRACE]" + message, _))

  final def debug(message: => String): UIO[Unit] =
    clock.currentDateTime >>= (log("[DEBUG]" + message, _))

  final def info(message: => String): UIO[Unit] =
    clock.currentDateTime >>= (log("[INFO]" + message, _))

  final def warning(message: => String): UIO[Unit] =
    clock.currentDateTime >>= (log("[WARN]" + message, _))

  final def error(message: => String): UIO[Unit] =
    clock.currentDateTime >>= (log("[ERROR]" + message, _))

  final def error(message: => String, cause: Cause[Any]): UIO[Unit] =
    clock.currentDateTime >>= (log("[ERROR]" + message + "\n" + cause.prettyPrint, _))
}

object CloudWatchLogger extends Serializable {
  val fromEnvironment: ZIO[Environment, Error, Logging[String]] =
    Task
      .mapN(
        awsAccessKeyId <*>
          awsSecretAccessKey <*>
          awsSessionToken <*>
          logGroupName <*>
          logStreamName <*>
          region
      ) { (accessKeyId, secretAccessKey, sessionToken, groupName, streamName, region) =>
        Task.effect {
          val credentials = AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken)
          CloudWatchLogsAsyncClient
            .builder()
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .region(region)
            .build
        }.map { client0 =>
          new Logging[String] {
            val logging: Logging.Service[Any, String] =
              new CloudWatchLogger {
                override val client        = client0
                override val logGroupName  = groupName
                override val logStreamName = streamName
                override val clock         = Clock.Live.clock
              }
          }
        }
      }
      .flatten
}
