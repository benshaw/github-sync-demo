package githubsync

import cats.MonadError
import cats.effect.Sync
import cats.syntax.all._
import githubsync.ErrorHandler.{HttpErrorHandler, RoutesHttpErrorHandler}
import githubsync.algebras.service._
import githubsync.domain.repositoryevent._
import githubsync.domain.starevent._
import io.chrisdavenport.log4cats.Logger
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}

object Routes {

  implicit def repositoryErrorHandler[F[_] : Logger : MonadError[*[_], RepositoryError]]: HttpErrorHandler[F, RepositoryError] with Http4sDsl[F] =
    new HttpErrorHandler[F, RepositoryError] with Http4sDsl[F] {
      private val handler: RepositoryError => F[Response[F]] = {
        case RepositoryDownStreamError(e) => Logger[F].error(e.getMessage) *> InternalServerError(e.getMessage.asJson)
        case DownStreamBadRequest(e, m) => Logger[F].error(e + m) *> BadRequest(m.asJson)
      }

      override def handle(routes: HttpRoutes[F]): HttpRoutes[F] =
        RoutesHttpErrorHandler(routes)(handler)
    }

  implicit def eventErrorHandler[F[_] : Logger : MonadError[*[_], EventError]]: HttpErrorHandler[F, EventError] with Http4sDsl[F] =
    new HttpErrorHandler[F, EventError] with Http4sDsl[F] {
      private val handler: EventError => F[Response[F]] = {
        case EventDownStreamError(e) => Logger[F].error(e.getMessage) *> InternalServerError(e.getMessage.asJson)
        case UnknownAction(a) => Logger[F].warn("Unknown Action: " + a) *> Ok()
      }

      override def handle(routes: HttpRoutes[F]): HttpRoutes[F] =
        RoutesHttpErrorHandler(routes)(handler)
    }

  def repositoryRoutes[F[_]](repository: RepositoryService[F])(implicit S: Sync[F], H: HttpErrorHandler[F, RepositoryError]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    val routes = HttpRoutes.of[F] {
      case GET -> Root / "org" / org / "starred" =>
        Ok(
          repository
            .stargazers(org)
            .map(_.map(_.asJson).getOrElse(s"Data for the requested Org $org not yet in persistent storage, synchronization begun".asJson))
        )
    }

    H.handle(routes)
  }

  def eventRoutes[F[_]](event: EventService[F])(implicit S: Sync[F], H: HttpErrorHandler[F, EventError]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    val routes = HttpRoutes.of[F] {
      case req@POST -> Root / "repo" / "event" => req.decode[RepositoryEvent] { e => Ok(event.repoEvent(e).map(_ => ())) }
      case req@POST -> Root / "star" / "event" => req.decode[StarEvent] { e => Ok(event.starEvent(e).map(_ => ())) }
    }

    H.handle(routes)
  }

}





