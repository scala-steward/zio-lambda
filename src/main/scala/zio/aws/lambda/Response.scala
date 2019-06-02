package zio.aws.lambda

import com.softwaremill.sttp.Response
import scala.util.Try

final case class LambdaResponse(
    /** The request ID, which identifies the request that triggered the function invocation. */
    requestId: String,
    /** The content of the response */
    body: String
) extends Serializable

object LambdaResponse extends Serializable {
}
