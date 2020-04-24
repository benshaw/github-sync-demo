package githubsync.domain

import fs2.Stream

object GitHubApi {
  trait GitHubApiAlgebra [F[_]]{
    def contributors(repo: Repository): Stream[F, User]
    def repositories(org: String): Stream[F, Repository]
    def starred(user: User): Stream[F, Repository]
    //def addStar(repository: GitHubRepository)
  }

  trait GitHubPersistentStoreAlgebra [F[_]] extends GitHubApiAlgebra[F] {
    def addContributors[A](repo: Repository, contributors:List[User]): F[A]
    def addRepositories[A](org: String, repos:   List[Repository]): F[A]
    def addStarred[A](user: User): F[A]
  }

  sealed trait GitHubApiError extends Exception
  case class UriParseError(e: String) extends GitHubApiError
  case class ErrorRetrievingJson(e: String) extends GitHubApiError
  case class JsonDecodingError(e: String) extends GitHubApiError
  case class ResourceNotFound(e: String) extends GitHubApiError
}
