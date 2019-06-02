package zio.aws.lambda

import scalaz.zio.ZIO
import scalaz.zio.clock.{ Clock, currentTime }
import java.util.concurrent.TimeUnit

final case class Context(
  functionName: String,
  functionVersion: String,
  functionArn: String,
  memoryLimitInMB: Int,
  requestId: String,
  logGroupName: String,
  logStreamName: String,
  identity: String,
  clientContext: String,
  deadline: Long
) {
  def remainingTime: ZIO[Clock, Nothing, Long] = currentTime(TimeUnit.MILLISECONDS) map (deadline - _)

  def logger: LambdaLogger[Any]
}