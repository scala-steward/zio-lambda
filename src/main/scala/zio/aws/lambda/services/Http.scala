package zio.aws.lambda.services

import sttp.client.{ Response => SResponse, _ }
import sttp.client.asynchttpclient.zio._
import sttp.model.Uri
import scala.concurrent.duration._
import scala.sys.process._
import zio.{ UIO, ZIO }
import zio.blocking.Blocking
import zio.IO
import zio.aws.lambda._

trait Http extends Serializable {
  def http: Http.Service[Any]
}

object Http extends Serializable {
  trait Service[R] extends Serializable {
    def close: UIO[Unit]
    def get(uri: Uri, headers: Map[String, String]): ZIO[R, Error, SResponse[String]]
    def post(uri: Uri, body: String, headers: Map[String, String]): ZIO[R, Error, SResponse[String]]
    def curl(verb: String, uri: Uri, args: String*): ZIO[R, Nothing, Unit]
  }

  trait Live extends Http with Blocking.Live {
    val http: Service[Any] = new Service[Any] {
      implicit val backend = AsyncHttpClientZioBackend(SttpBackendOptions(30.minute, None))

      val close = UIO.effectTotal(backend.close())

      def get(uri: Uri, headers: Map[String, String]) =
        sttp.headers(headers).get(uri).send[ZIO[Any, Throwable, ?]]().mapError(HttpError(_))

      def post(uri: Uri, body: String, headers: Map[String, String]) =
        sttp.headers(headers).body(body).post(uri).send().mapError(HttpError(_))

      def curl(verb: String, uri: Uri, args: String*) =
        IO.effectAsync[Any, Nothing, String] { k =>
            blocking.effectBlocking {
              Seq("curl", "-L", "-sS", s"-X${verb}", uri.toString) ++ args !! ProcessLogger(s => k(UIO.succeed(s)))
            }
          }
          .orDie
          .unit
    }
  }

  object Live extends Live
}
