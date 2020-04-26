package githubsync

import githubsync.ErrorHandler.HttpErrorHandler
import githubsync.algebras.github.GitHubApiError
import githubsync.domain.user._
import githubsync.domain.repository._
import cats.Applicative
import cats.effect.Sync
import cats.syntax.all._
import githubsync.algebras.service.RepositoryService
import githubsync.interpreters.service.repositoryserviceinterpreter
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes}

object Routes {

  def contributorRoutes[F[_]](service: RepositoryService[F])(implicit S:Sync[F], H: HttpErrorHandler[F, GitHubApiError]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    //! \todo cleanup
    val routes = HttpRoutes.of[F] {
      case GET -> Root / "org" / org / "starred" =>
        Ok(service.stargazers(org).map(_.asJson))
    }

    H.handle(routes)
  }

  implicit private val userEncoder: Encoder[User] = deriveEncoder[User]
  implicit private def userEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[User]] = jsonEncoderOf
  implicit private val repoEncoder: Encoder.AsObject[Repository] = deriveEncoder[Repository]
  implicit private def repoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Repository]] = jsonEncoderOf

}





