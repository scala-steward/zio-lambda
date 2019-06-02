package zio.aws.lambda

import com.softwaremill.sttp.Response

sealed abstract class Error extends Product with Serializable { self =>
  final def toThrowable: Throwable = self match {
    case MissingEnv(keys)       => new Throwable(s"""Environment variables ${keys.mkString(",")} were missing""")
    case MalformedRequest(resp) => new Throwable(s"Invocation was missing some headers: " ++ resp.headers.mkString(","))
    case HttpError(origin)      => new Throwable("An error occurred when calling lambda", origin)
  }
}
final case class MissingEnv(keys: ::[String])        extends Error
final case class MalformedRequest(resp: Response[_]) extends Error
final case class HttpError(origin: Throwable)        extends Error
