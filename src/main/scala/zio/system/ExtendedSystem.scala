package zio.system

import java.net.URI
import zio.IO
import zio.stream.StreamChunk

trait ExtendedSystem extends System.Service[Any] {
  import ExtendedSystem.CurlOpts

  def curlGet(url: URI, options: CurlOpts = CurlOpts.default): IO[Throwable, StreamChunk[Throwable, Byte]]
  def curlPost(url: URI, options: CurlOpts = CurlOpts.default): IO[Throwable, StreamChunk[Throwable, Byte]]
}

object ExtendedSystem extends Serializable {
  trait Live extends System {
    val system: System.Service[Any] = new ExtendedSystem {
      def curlGet(url: URI, options: Any): Any = ???

      def curlPost(url: URI, options: Any): Any = ???
    }
  }

  final case class CurlOpts(
      followRedirects: Boolean
  ) extends Product
      with Serializable

  object CurlOpts {
    final val default = CurlOpts(true)
  }
}
