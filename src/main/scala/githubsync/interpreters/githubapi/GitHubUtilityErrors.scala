package githubsync.interpreters.githubapi

import cats.effect.Sync
import cats.implicits._
import githubsync.domain.GitHubApi._
import org.http4s._

//! Raise errors using the GitHubError ADT
object GitHubUtilityErrors {

  implicit class JsonError[F[_]](r: Response[F])(implicit F: Sync[F]) {

    def attempt[T](f: DecodeFailure => GitHubApiError)(implicit d: EntityDecoder[F, T]): F[T] =
      r.attemptAs[T]
        .leftMap(f(_))
        .value
        .flatMap {
          case Right(a) => F.pure(a)
          case Left(e) => F.raiseError(e)
        }

  }

  implicit class UriError[F[_]](s: String)(implicit F: Sync[F]) {
    def asUri(): F[Uri] =
      Uri.fromString(s) match {
        case Right(s) => F.pure(s)
        case Left(e) => F.raiseError(UriParseError(e.message))
      }
  }


}
