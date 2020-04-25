package githubsync.domain

import cats.{Foldable, Monad}
import fs2.Stream

object GitHubApi {
  trait GitHubApiAlgebra [F[_]]{
    //def contributors(repo: Repository): Stream[F, User]
    def repositories(org: String): Stream[F, Repository]
    //def starred(user: User): Stream[F, Repository]
    def stargazers(repo: Repository): Stream[F, User]
    //def addStar(repository: GitHubRepository)
  }

  trait GitHubPersistentStoreAlgebra [F[_]] extends GitHubApiAlgebra[F] {

    def addStarGazers[C[_]: Foldable : Monad](c: C[User]): F[Int]
    def addRepositories[C[_]: Foldable : Monad](c: C[Repository]): F[Int]
    def registered(org: String): Stream[F, String]
    def register(org: String): F[Int]

      //def addContributors[A](repo: Repository, contributors:List[User]): F[A]
    //def addRepositories(r: Stream[F, Repository]): Stream[Any, Int]
      //def addRepositories[A](repos:   Stream[F, Repository]): F[A]
    //def addStarred[A](user: User): F[A]
  }

  sealed trait GitHubApiError extends Exception
  case class UriParseError(e: String) extends GitHubApiError
  case class ErrorRetrievingJson(e: String) extends GitHubApiError
  case class JsonDecodingError(e: String) extends GitHubApiError
  case class ResourceNotFound(e: String) extends GitHubApiError
}
