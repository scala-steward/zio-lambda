package zio.aws.lambda

import sttp.client._
import zio._
import zio.clock.Clock
import zio.duration.Duration
import zio.system.System
import java.util.concurrent.TimeUnit
import zio.aws.lambda.services._

/**
 * A `ZLambda[R]` is a purely functional AWS Lambda function that runs on top of [ZIO](https://zio.dev/)
 */
abstract class ZLambda extends App with CustomRuntimeApi {

  ////
  // Abstract members

  /** The resources that lambda function needs in order to perform its action */
  type Resources

  /** Acquires function-specific resources (database clients, etc.) */
  def resources: ZIO[Runtime, Error, Resources]

  /** Releases function-specific resources */
  def cleanup(r: Resources): ZIO[Runtime, Nothing, Unit]

  /** Performs the action  */
  def handle(req: Request, res: Resources): ZIO[Runtime, Error, Response]

  ////
  // Main

  /**
   * Runs the lambda by going through the following steps:
   * - Acquires necessary resources
   * - enters the invocation loop:
   *   - get an invocation request
   *   - executes the handler
   *   - returns the response
   *
   * @see https://docs.aws.amazon.com/lambda/latest/dg/runtimes-custom.html
   */
  final private def main(args: List[String]) = ZManaged.make(acquire)(release).use {
    case (res, rt) =>
      (for {
        req      <- invocationRequest
        _        <- setTraceId(req.traceId)
        response <- handle(req, res).timeout(Duration(req.deadline, TimeUnit.MILLISECONDS))
        _ <- response match {
              case Some(res) => invocationResponse(req, res)
              case None      => invocationError(req, "Function timed out")
            }
      } yield ()).forever.provide(rt)
  }

  /**
   * Acquires all the resources necessary for the lambda to run, runtime included. Function-specific
   * resources can use the lambda runtime to acquire its own resources.
   */
  final val acquire: ZIO[System, Error, (Resources, Runtime)] =
    (for {
      env    <- zio.aws.lambda.environment.Environment.fromEnvironment
      logger <- Logging.fromCloudWatch.provide(env)
    } yield Runtime(env.lambdaEnv, logger.logger)) >>= (
      rt => resources.provide(rt).map((_, rt))
    )

  /**
   * Releases all resources, starting with the function-specific ones
   */
  final def release(env: (Resources, Runtime)): UIO[Unit] = cleanup(env._1).provide(env._2) *> env._2.http.close
}

private[lambda] trait CustomRuntimeApi {
  final val version = "2018-06-01"

  final val invocationRequest: ZIO[Runtime, Error, Request] = for {
    rapi       <- runtimeApi
    resp       <- httpGet(uri"http://${rapi}/${version}/runtime/invocation/next")
    invocation <- IO.fromOption(Request.fromHttpResponse(resp)) mapError (_ => MalformedRequest(resp))
  } yield invocation

  final def invocationResponse(req: Request, res: Response): ZIO[Runtime, Error, Unit] =
    (runtimeApi >>= (
      rapi => httpPost(uri"http://${rapi}/${version}/runtime/invocation/${req.requestId}/response", res.body).unit
    ))

  final def invocationError(req: Request, err: String): ZIO[Runtime, Error, Unit] =
    (runtimeApi >>= (
      rapi => httpPost(uri"http://${rapi}/${version}/runtime/invocation/${req.requestId}/error", err).unit
    ))

  final def initError(err: String): ZIO[Runtime, Error, Unit] =
    runtimeApi >>= (rapi => curl("POST", uri"http://${rapi}/${version}/runtime/init/error", err))
}
