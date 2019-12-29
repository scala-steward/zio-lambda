package zio.aws.lambda

import zio.clock.Clock
import zio.aws.lambda.services._
import zio.aws.lambda.environment.Environment

trait Runtime extends Clock {
  def env: Environment.Service[Any]
  def http: Http.Service[Any]
  def logger: Logging.Service[Any]
}

object Runtime {
  def apply(env0: Environment.Service[Any], logger0: Logging.Service[Any]): Runtime =
    new Runtime {
      val env    = env0
      val http   = Http.Live.http
      val logger = logger0
      val clock  = Clock.Live.clock
    }
}
