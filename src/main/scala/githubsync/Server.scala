package githubsync

import githubsync.domain.{GitHubApi, RepositoryService}
import githubsync.interpreters.upstream.GitHubApiInterpreter.{GitHubApiConfig, GitHubToken, GitHubUri}
import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import com.olegpy.meow.hierarchy._

import scala.concurrent.ExecutionContext.global
import org.http4s.implicits._
import cats.implicits._
import ciris._
import ciris.refined._
import eu.timepit.refined.auto._
import githubsync.domain.GitHubApi.{GitHubApiAlgebra, GitHubPersistentStoreAlgebra}
import githubsync.interpreters.persistent.DoobiePersistentStoreInterpreter
import githubsync.interpreters.upstream.GitHubApiInterpreter


object Server {

  case class ServerConfig(appLogging: Boolean, clientLogging:Boolean, gitHubApiConfig: GitHubApiConfig)

  val config:ConfigValue[ServerConfig] =
    (env("GH_URL").as[GitHubUri].default("https://api.github.com"),
      env("GH_TOKEN").as[GitHubToken].option,
      env("LOG_APP").as[Boolean].default(false),
      env("LOG_CLI").as[Boolean].default(false)).parMapN{(uri,token, app, cli) =>
      ServerConfig(
        app,
        cli,
        GitHubApiConfig(uri, token))
    }



  def stream[F[_]: ConcurrentEffect](conf: ServerConfig)(implicit T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {

    for {
      client <- BlazeClientBuilder[F](global).stream
      loggedClient = if(conf.clientLogging){ org.http4s.client.middleware.Logger(true, true, _ => false)(client)} else client
      api: GitHubApiAlgebra[F] = GitHubApiInterpreter.create(loggedClient, conf.gitHubApiConfig)
      db: GitHubPersistentStoreAlgebra[F] = DoobiePersistentStoreInterpreter.create(Database.xa)
      contributorsService = RepositoryService.create(api,db)

      httpApp = (
        Routes.contributorRoutes[F](contributorsService)
      ).orNotFound

      loggedHttpApp = if(conf.appLogging){Logger.httpApp(true, true)(httpApp)} else httpApp

      exitCode <- BlazeServerBuilder[F]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(loggedHttpApp)
        .serve
    } yield exitCode
  }.drain
}