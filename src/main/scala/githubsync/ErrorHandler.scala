package githubsync

import cats.MonadError
import cats.data.{Kleisli, OptionT}
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.dsl.Http4sDsl

import cats.implicits._
import org.http4s.circe._
import io.circe.syntax._
import githubsync.domain.GitHubApi._

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

  implicit def gitHubErrorHandler[F[_] : MonadError[*[_], GitHubApiError]]: HttpErrorHandler[F, GitHubApiError] = new HttpErrorHandler[F, GitHubApiError] with Http4sDsl[F] {
    private val handler: GitHubApiError => F[Response[F]] = {
      case UriParseError(e) => BadRequest(e.asJson)
      case ErrorRetrievingJson(e) => InternalServerError(e.asJson)
      case JsonDecodingError(e) => InternalServerError(e.asJson)
      case ResourceNotFound(e) => BadRequest(e.asJson)
    }

    override def handle(routes: HttpRoutes[F]): HttpRoutes[F] =
      RoutesHttpErrorHandler(routes)(handler)
  }

}