package githubsync

import cats.MonadError
import cats.data.{Kleisli, OptionT}
import cats.implicits._
import org.http4s.{HttpRoutes, Request, Response}

object ErrorHandler {

  trait HttpErrorHandler[F[_], E <: Throwable] {
    def handle(routes: HttpRoutes[F]): HttpRoutes[F]
  }

  object RoutesHttpErrorHandler {
    def apply[F[_], E <: Throwable](routes: HttpRoutes[F])(handler: E => F[Response[F]])(implicit M: MonadError[F, E]): HttpRoutes[F] =
      Kleisli { req: Request[F] =>
        OptionT {
          routes.run(req).value.handleErrorWith { e => handler(e).map(Option(_)) }
        }
      }
  }
}
