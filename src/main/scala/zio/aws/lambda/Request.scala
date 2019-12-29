package zio.aws.lambda

import sttp.client.{ Response => SResponse }

final case class Request(
  /** The request ID, which identifies the request that triggered the function invocation. */
  requestId: String,
  /** The date that the function times out in Unix time milliseconds. */
  deadline: Long,
  /** The ARN of the Lambda function, version, or alias that's specified in the invocation. */
  functionArn: String,
  /** The AWS X-Ray tracing header */
  traceId: String,
  /** For invocations from the AWS Mobile SDK, data about the client application and device */
  clientContext: String,
  /** For invocations from the AWS Mobile SDK, data about the Amazon Cognito identity provide */
  identity: String,
  /** The content of the request */
  body: String
) extends Serializable

object Request extends Serializable {
  def fromHttpResponse(r: SResponse[String]): Option[Request] =
    for {
      requestId     <- r.header("Lambda-Runtime-Aws-Request-Id")
      deadline      <- r.header("Lambda-Runtime-Deadline-Ms").map(_.toLong)
      functionArn   <- r.header("Lambda-Runtime-Invoked-Function-Arn")
      traceId       <- r.header("Lambda-Runtime-Trace-Id")
      clientContext <- r.header("Lambda-Runtime-Client-Context")
      identity      <- r.header("Lambda-Runtime-Cognito-Identity")
      body          <- r.body.toOption
    } yield Request(
      requestId = requestId,
      deadline = deadline,
      functionArn = functionArn,
      traceId = traceId,
      clientContext = clientContext,
      identity = identity,
      body = body
    )
}
