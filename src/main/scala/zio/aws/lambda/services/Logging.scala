package zio.aws.lambda.services

trait Logging extends Serializable {
  def logger: Logging.Service[Any]
}

object Logging extends Serializable {
  trait Service extends Serializable {
    def debug(msg: String): IO[Unit]
    def info(msg: String): IO[Unit]
    def warn(msg: String): IO[Unit]
    def error(msg: String): IO[Unit]
    def fatal(msg: String): IO[Unit]
  }
}