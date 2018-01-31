package sloth

import sloth.core._
import sloth.client.RequestTransport

import _root_.mycelium.client._

import scala.concurrent.{ExecutionContext, Future}

package object mycelium {

  implicit class TransportableWebsocketClient[PickleType, ErrorType](client: WebsocketClient[_, PickleType, _, ErrorType]) {

    def toTransport(behaviour: SendBehaviour, recover: PartialFunction[Throwable, ErrorType] = PartialFunction.empty)(implicit ec: ExecutionContext) = new RequestTransport[PickleType, Lambda[T => Future[Either[ErrorType, T]]]] {
      def apply(request: Request[PickleType]): Future[Either[ErrorType, PickleType]] = client.send(request.path, request.payload, behaviour).recover(recover andThen Left.apply)
    }

    def toTransport(behaviour: SendBehaviour, onError: ErrorType => Throwable)(implicit ec: ExecutionContext) = new RequestTransport[PickleType, Future] {
      def apply(request: Request[PickleType]): Future[PickleType] = {
        client.send(request.path, request.payload, behaviour).flatMap {
          case Right(res) => Future.successful(res)
          case Left(err) => Future.failed(onError(err))
        }
      }
    }
  }
}
