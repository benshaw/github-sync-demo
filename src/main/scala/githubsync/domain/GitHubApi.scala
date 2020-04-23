package githubsync.domain

object GitHubApi {
  trait GitHubApiAlgebra [F[_]]{
    def contributors(repo: Repository): F[List[User]]
    def repositories(org: String): F[List[Repository]]
    def starred(user: User): F[List[Repository]]
  }

  sealed trait GitHubApiError extends Exception
  case class UriParseError(e: String) extends GitHubApiError
  case class ErrorRetrievingJson(e: String) extends GitHubApiError
  case class JsonDecodingError(e: String) extends GitHubApiError
  case class ResourceNotFound(e: String) extends GitHubApiError
}
