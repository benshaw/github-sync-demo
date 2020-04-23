package githubsync.interpreters.githubapi

import cats.effect.Sync
import cats.implicits._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.MinSize
import githubsync.domain.GitHubApi.GitHubApiAlgebra
import githubsync.domain.{Repository, User}
import githubsync.interpreters.PaginatedClientWithGithubErrors
import githubsync.interpreters.githubapi.models.{GitHubContributor, GitHubLicense, GitHubOwner, GitHubPermissions, GitHubRepository, GitHubStarredRepository}
import io.circe.Decoder
import io.circe.generic.semiauto._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s._

object GitHubApiInterpreter {

  import GitHubUtilityErrors._
  import PaginatedClientWithGithubErrors._

  type GitHubUri = String Refined eu.timepit.refined.string.Uri
  type GitHubToken = String Refined MinSize[5]// \todo refine the token better find out the exact format ? is it a jwt
  case class GitHubApiConfig(apiUrl:GitHubUri , token:Option[GitHubToken])

  def create[F[_] : Sync](client: Client[F], config: GitHubApiConfig): GitHubApiAlgebra[F] =
    new GitHubApiAlgebra[F] {

      private val authHeaders: Headers = config.token.map(t => Headers.of(Header("Authorization", s"token ${t}"))).getOrElse(Headers.empty)

      def contributors(r: Repository): F[List[User]] = for {
        uri <- s"${config.apiUrl}/repos/${r.owner}/${r.name}/contributors".asUri()
        r <- client.getAllPages[GitHubContributor](Request[F](method = Method.GET, uri = uri, headers = authHeaders))
      } yield r.map(a => User(a.login, a.contributions))

      def repositories(org: String): F[List[Repository]] = for {
        uri <- s"${config.apiUrl}/orgs/$org/repos".asUri()
        r <- client.getAllPages[GitHubRepository](Request[F](method = Method.GET, uri = uri, headers = authHeaders))
      } yield r.map(a => Repository(a.name, a.owner.login))

      def starred(user: User): F[List[Repository]] = for {
        uri <- s"${config.apiUrl}/users/${user.name}/starred".asUri()
        r <- client.getAllPages[GitHubStarredRepository](Request[F](method = Method.GET, uri = uri, headers = authHeaders))
      } yield r.map(a => Repository(a.name, a.owner.login))
    }


}
