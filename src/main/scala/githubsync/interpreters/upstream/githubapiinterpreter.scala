package githubsync.interpreters.upstream

import cats.effect.Sync
import cats.implicits._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.MinSize
import fs2.{Pipe, Stream}
import githubsync.algebras.github.GitHubApiAlgebra
import githubsync.domain.repository._
import githubsync.domain.user._
import githubsync.interpreters.upstream.domain._
import io.chrisdavenport.log4cats.Logger
import io.circe.Json
import org.http4s._
import org.http4s.client.Client

object githubapiinterpreter {

  import githubutilityerrors._
  import paginatedclient._

  type CallbackUrl = String Refined eu.timepit.refined.string.Uri
  type GitHubUri = String Refined eu.timepit.refined.string.Uri
  type GitHubToken = String Refined MinSize[5] // \todo refine the token better find out the exact format ? is it a jwt
  case class GitHubApiConfig(apiUrl: GitHubUri, token: Option[GitHubToken], callBackUrl: CallbackUrl)

  def GitHubApiInterpreter[F[_] : Sync : Logger](client: Client[F], config: GitHubApiConfig): GitHubApiAlgebra[F] =
    new GitHubApiAlgebra[F] {

      private val authHeaders: Headers = config.token.map(t => Headers.of(Header("Authorization", s"token ${t}"))).getOrElse(Headers.empty)

      def repositories(org: String): Stream[F, Repository] = for {
        uri <- s"${config.apiUrl}/orgs/$org/repos".asUriStream()
        a <- client.getAllPages[GitHubRepository](Request[F](method = Method.GET, uri = uri, headers = authHeaders))
        r = Repository(a.name, org)
      } yield r

      def stargazers(repo: Repository): Stream[F, User] = for {
        uri <- s"${config.apiUrl}/repos/${repo.owner}/${repo.name}/stargazers".asUriStream()
        a <- client.getAllPages[GitHubStarGazer](Request[F](method = Method.GET, uri = uri, headers = authHeaders))
        r = User(a.login, repo.name)
      } yield r

      def registerForStarEvents(repo: Repository): F[Repository] = {
        val c = GitHubWebHookRegistrationConfig(url = s"${config.callBackUrl}/star/event",
          content_type = "json",
          insecure_ssl = "1") //! set this to 0 if SSL is configured

        val wh = GitHubWebHookRegistration(name = s"web",
          active = true,
          events = List("star"),
          config = c)

        webhook(wh, repo).map(_ => repo)
      }

      def registerForRepoEvents(repo: Repository): F[Repository] = {
        val c = GitHubWebHookRegistrationConfig(url = s"${config.callBackUrl}/repo/event",
          content_type = "json",
          insecure_ssl = "1") //! set this to 0 if SSL is configured

        val wh = GitHubWebHookRegistration(name = s"web",
          active = true,
          events = List("repository"),
          config = c)

        webhook(wh, repo).map(_ => repo)
      }

      private def webhook(webHook: GitHubWebHookRegistration, repo: Repository): F[GitHubWebHookResponse] = for {
        uri <- s"${config.apiUrl}/repos/${repo.owner}/${repo.name}/hooks".asUri()
        r <- client.expect[GitHubWebHookResponse](Request[F](method = Method.POST, uri = uri, headers = authHeaders).withEntity(webHook))
      } yield r

      implicit val ghsrd: Pipe[F, Json, GitHubStarredRepository] = jsonDecoder[F, GitHubStarredRepository]
      implicit val ghrd: Pipe[F, Json, GitHubRepository] = jsonDecoder[F, GitHubRepository]
      implicit val ghsg: Pipe[F, Json, GitHubStarGazer] = jsonDecoder[F, GitHubStarGazer]
    }


}
