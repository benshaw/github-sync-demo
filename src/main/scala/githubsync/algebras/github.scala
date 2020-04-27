package githubsync.algebras

import cats.{Foldable, Monad}
import fs2.Stream
import githubsync.domain.repository._
import githubsync.domain.user._

object github {

  sealed trait GitHubAlgebra[F[_]]{
    def repositories(org: String): Stream[F, Repository]
    def stargazers(repo: Repository): Stream[F, User]
  }

  trait GitHubApiAlgebra[F[_]] extends GitHubAlgebra[F]{
    def registerForRepoEvents(repo: Repository): F[Repository]
    def registerForStarEvents(repo: Repository): F[Repository]
  }

  trait GitHubPersistentStoreAlgebra[F[_]] extends GitHubAlgebra[F]{
    def addStarGazers[C[_] : Foldable : Monad](c: C[User]): F[C[User]]
    def deleteStarGazer(u: User):F[Int]
    def addRepositories[C[_] : Foldable : Monad](c: C[Repository]): F[C[Repository]]
    def deleteRepository(r: Repository): F[Int]
    def registered(org: String): Stream[F, String]
    def register(org: String): F[Int]
  }

  sealed trait GitHubApiError extends Exception
  case class UriParseError(e: String) extends GitHubApiError
  case class ErrorRetrievingJson(e: String) extends GitHubApiError
  case class JsonDecodingError(e: String) extends GitHubApiError
  case class ResourceNotFound(e: String) extends GitHubApiError

  sealed trait PersistentStoreError extends Exception
  case class SelectionError(e: Throwable) extends PersistentStoreError
  case class InsertionError(e: Throwable) extends PersistentStoreError

}
