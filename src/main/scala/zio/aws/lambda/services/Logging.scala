package zio.aws.lambda.services

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsAsyncClient
import software.amazon.awssdk.services.cloudwatchlogs.model.{ PutLogEventsRequest, InputLogEvent }
import software.amazon.awssdk.auth.credentials.{StaticCredentialsProvider, AwsSessionCredentials}
import scalaz.zio.{ Task, ZIO }
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
      def log(msg: String) = Task.fromFutureJava { () =>
        val request = PutLogEventsRequest.builder()
          .logGroupName(logGroupName)
          .logStreamName(logStreamName)
          .logEvents(InputLogEvent.builder().message(msg).build)
          .build
        client.putLogEvents(request)
      }.orDie.unit
    }
  }

  val fromCloudWatch: ZIO[Environment, Error, Logging] = for {
    accessKeyId <- ZIO.access[Environment](_.lambdaEnv.awsAccessKeyId)
    secretAccessKey <- ZIO.access[Environment](_.lambdaEnv.awsSecretAccessKey)
    sessionToken <- ZIO.access[Environment](_.lambdaEnv.awsSessionToken)
    logGroupName <- ZIO.access[Environment](_.lambdaEnv.logGroupName)
    logStreamName <- ZIO.access[Environment](_.lambdaEnv.logStreamName)
    region <- ZIO.access[Environment](_.lambdaEnv.region)
    client <- Task.effect {
      val credentials = AwsSessionCredentials.create(accessKeyId, secretAccessKey, sessionToken)
      CloudWatchLogsAsyncClient.builder()
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .region(region)
        .build // TODO create a custom Http client?
    }.catchAll(t => ZIO.fail(CloudWatchError(t)))
  } yield new CloudWatchLogging(client, logGroupName, logStreamName)
}