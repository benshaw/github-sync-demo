package githubsync.interpreters.upstream

import cats.effect.Sync
import cats.implicits._
import githubsync.algebras.github._
import org.http4s._
import fs2.{Pipe, Pure, RaiseThrowable, Stream, text}
import io.chrisdavenport.log4cats.Logger


//! Raise errors using the GitHubError ADT
object githubutilityerrors {

  implicit class JsonError[F[_]: Logger](r: Response[F])(implicit F: Sync[F]) {

    def attempt[T](f: DecodeFailure => GitHubApiError)(implicit d: EntityDecoder[F, T]): F[T] =
      r.attemptAs[T]
        .leftMap(f(_))
        .value
        .flatMap {
          case Right(a) => F.pure(a)
          case Left(e) => F.raiseError[T](e)
        }
        .onError {
          case _ => Logger[F].error("Error Parsing Json")
        }
  }

  implicit class UriError[F[_]: Logger](s: String)(implicit F: Sync[F]) {

    def asUriStream(): Stream[F, Uri] =
      Stream.eval(asUri())

    def asUri(): F[Uri] = {
      val uri: F[Uri] = Uri.fromString(s) match {
        case Right(s) => F.pure(s)
        case Left(e) => F.raiseError(UriParseError(e.message))
      }

      uri.onError {
        case _ => Logger[F].error(s"Error Parsing URL")
      }
    }



  }


}
