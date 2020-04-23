package githubsync

import githubsync.ErrorHandler.HttpErrorHandler
import githubsync.domain.GitHubApi.GitHubApiError
import githubsync.domain.{Repository, StarredRepositoriesService, User}
import cats.Applicative
import cats.effect.Sync
import cats.syntax.all._
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes}

object Routes {

  def contributorRoutes[F[_]](starred: StarredRepositoriesService[F])(implicit S:Sync[F], H: HttpErrorHandler[F, GitHubApiError]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    val routes = HttpRoutes.of[F] {
      case GET -> Root / "org" / org / "starred" =>
        for {
          contributors <- starred.get.run(org)
          resp <- Ok(contributors.asJson)
        } yield resp
    }

    H.handle(routes)
  }

  implicit private val userEncoder: Encoder[User] = deriveEncoder[User]
  implicit private def userEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[User]] = jsonEncoderOf
  implicit private val repoEncoder: Encoder.AsObject[Repository] = deriveEncoder[Repository]
  implicit private def repoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Repository]] = jsonEncoderOf

}





