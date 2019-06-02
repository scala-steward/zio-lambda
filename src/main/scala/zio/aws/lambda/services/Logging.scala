package zio.aws.lambda.services

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsAsyncClient
import software.amazon.awssdk.services.cloudwatchlogs.model.{ PutLogEventsRequest, InputLogEvent }
import software.amazon.awssdk.auth.credentials.{StaticCredentialsProvider, AwsSessionCredentials}
import scalaz.zio.Task
import scalaz.zio.interop.javaconcurrent._
import zio.aws.lambda._

trait Logging extends Serializable {
  def logger: Logging.Service[Any]
}

object Logging extends Serializable {
  trait Service[R] extends Serializable {
    def log(msg: String): ZIO[R, Nothing, Unit]

    def debug(msg: String): ZIO[R, Nothing, Unit] = log("[DEBUG] " ++ msg)
    def info(msg: String): ZIO[R, Nothing, Unit] = log("[INFO] " ++ msg)
    def warn(msg: String): ZIO[R, Nothing, Unit] = log("[WARN] " ++ msg)
    def error(msg: String): ZIO[R, Nothing, Unit] = log("[ERROR] " ++ msg)
    def fatal(msg: String): ZIO[R, Nothing, Unit] = log("[FATAL] " ++ msg)
  }

  class CloudWatchLogging(client: CloudWatchLogsAsyncClient, logGroupName: String, logStreamName: String) extends Logging {
    final val logger = new Service[Any] {
      def log(msg: String) = Task.fromFutureJava { _ =>
        val request = PutLogEventsRequest.builder()
          .logGroupName(logGroupName)
          .logStreamName(logStreamName)
          .logEvents(InputLogEvent.builder().message(msg))
          .build
        client.putLogEvents(request)
      }.orDie.unit
    }
  }

  val fromCloudWatch: ZIO[Environment, Throwable, Logging] = for {
    accessKeyId <- awsAccessKeyId
    secretAccessKey <- awsSecretAccessKey
    sessionToken <- awsSessionToken
    logGroupName <- logGroupName
    logStreamName <- logStreamName
    region <- region
    client <- IO.effect {
      val credentials = AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken)
      CloudWatchLogsAsyncClient.builder()
        .withCredentialsProvider(StaticCredentialsProvider.create(credentials))
        .withRegion(region)
        .build // TODO create a custom Http client?
    }
  } yield new CloudWatchLogging(client, logGroupName, logStreamName)
}