package zio.aws.lambda

import sttp.client.{ Response => SResponse }

sealed abstract class Error extends Product with Serializable { self =>
  final def toThrowable: Throwable = self match {
    case MissingEnv(keys)        => new Throwable(s"""Environment variables ${keys.mkString(",")} were missing""")
    case MalformedRequest(resp)  => new Throwable(s"Invocation was missing some headers: " ++ resp.headers.mkString(","))
    case HttpError(origin)       => new Throwable("An error occurred when calling lambda", origin)
    case CloudWatchError(origin) => new Throwable("An error occurred when initialising logging to CloudWatch", origin)
  }
}
final case class MissingEnv(keys: ::[String])         extends Error
final case class MalformedRequest(resp: SResponse[_]) extends Error
final case class HttpError(origin: Throwable)         extends Error
final case class CloudWatchError(origin: Throwable)   extends Error
