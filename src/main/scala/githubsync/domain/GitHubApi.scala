package githubsync.domain

import cats.{Foldable, Monad}
import fs2.Stream

object GitHubApi {
  trait GitHubApiAlgebra [F[_]]{
    def repositories(org: String): Stream[F, Repository]
    def stargazers(repo: Repository): Stream[F, User]
  }

  trait GitHubPersistentStoreAlgebra [F[_]] extends GitHubApiAlgebra[F] {
    def addStarGazers[C[_]: Foldable : Monad](c: C[User]): F[Int]
    def addRepositories[C[_]: Foldable : Monad](c: C[Repository]): F[Int]
    def registered(org: String): Stream[F, String]
    def register(org: String): F[Int]
  }

  sealed trait GitHubApiError extends Exception
  case class UriParseError(e: String) extends GitHubApiError
  case class ErrorRetrievingJson(e: String) extends GitHubApiError
  case class JsonDecodingError(e: String) extends GitHubApiError
  case class ResourceNotFound(e: String) extends GitHubApiError
}
