package githubsync.interpreters.upstream

import cats.effect.Sync
import cats.implicits._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.MinSize
import githubsync.algebras.github.GitHubApiAlgebra
import githubsync.domain.repository._
import githubsync.domain.user._
import githubsync.interpreters.upstream.domain.{GitHubContributor, GitHubLicense, GitHubOwner, GitHubPermissions, GitHubRepository, GitHubStarGazer, GitHubStarredRepository}
import io.circe.{Decoder, Json}
import io.circe.generic.semiauto._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s._
import fs2.{Pipe, Pure, RaiseThrowable, Stream, text}

object GitHubApiInterpreter {

  import GitHubUtilityErrors._
  import PaginatedClientWithGithubErrors._

  type GitHubUri = String Refined eu.timepit.refined.string.Uri
  type GitHubToken = String Refined MinSize[5]// \todo refine the token better find out the exact format ? is it a jwt
  case class GitHubApiConfig(apiUrl:GitHubUri , token:Option[GitHubToken])

  def create[F[_] : Sync: RaiseThrowable](client: Client[F], config: GitHubApiConfig): GitHubApiAlgebra[F] =
    new GitHubApiAlgebra[F] {

      private val authHeaders: Headers = config.token.map(t => Headers.of(Header("Authorization", s"token ${t}"))).getOrElse(Headers.empty)

      /*def contributors(r: Repository): Stream[F, User] = for {
          uri <- s"${config.apiUrl}/repos/${r.owner}/${r.name}/contributors".asUri()
          a <- client.getAllPages[GitHubContributor](Request[F](method = Method.GET, uri = uri, headers = authHeaders))
          r = User(a.login)
        } yield r*/

      def repositories(org: String): Stream[F, Repository] = for {
        uri <- s"${config.apiUrl}/orgs/$org/repos".asUri()
        a <- client.getAllPages[GitHubRepository](Request[F](method = Method.GET, uri = uri, headers = authHeaders))
        r = Repository(a.name, a.owner.login)
      } yield r

      def stargazers(repo: Repository): Stream[F, User] = for {
        uri <- s"${config.apiUrl}/repos/${repo.owner}/${repo.name}/stargazers".asUri()
        a <- client.getAllPages[GitHubStarGazer](Request[F](method = Method.GET, uri = uri, headers = authHeaders))
        r = User(a.login, repo.name)
      } yield r




      /*
      def starred(user: User): Stream[F, Repository] = (for {
        uri <- s"${config.apiUrl}/users/${user.name}/starred".asUri()
        a <- client.getAllPages[GitHubStarredRepository](Request[F](method = Method.GET, uri = uri, headers = authHeaders))
        r = a.name.flatMap(n => a.owner.login.map(l => Repository(n,l)))
      } yield r).collect { case Some(i) => i }*/

      implicit val ghsrd: Pipe[F, Json, GitHubStarredRepository] = jsonDecoder[F, GitHubStarredRepository]
      implicit val ghrd: Pipe[F, Json, GitHubRepository] = jsonDecoder[F, GitHubRepository]
      implicit val ghcrd: Pipe[F, Json, GitHubContributor] = jsonDecoder[F, GitHubContributor]
      implicit val ghsg: Pipe[F, Json, GitHubStarGazer] = jsonDecoder[F, GitHubStarGazer]
    }


}
