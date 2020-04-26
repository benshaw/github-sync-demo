package githubsync

import githubsync.interpreters.upstream.githubapiinterpreter.{CallbackUrl, GitHubApiConfig, GitHubToken, GitHubUri}
import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware
import com.olegpy.meow.hierarchy._

import scala.concurrent.ExecutionContext.global
import org.http4s.implicits._
import cats.implicits._
import ciris._
import ciris.refined._
import eu.timepit.refined.auto._
import githubsync.algebras.github.{GitHubApiAlgebra, GitHubPersistentStoreAlgebra}
import githubsync.interpreters.persistent.doobiepersisteninterpreter
import githubsync.interpreters.service.repositoryserviceinterpreter
import githubsync.interpreters.upstream.githubapiinterpreter
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger


object Server {

  case class ServerConfig(appLogging: Boolean, clientLogging:Boolean, gitHubApiConfig: GitHubApiConfig)

  val config:ConfigValue[ServerConfig] =
    (env("GH_URL").as[GitHubUri].default("https://api.github.com"),
      env("GH_TOKEN").as[GitHubToken].option,
      env("HOST").as[CallbackUrl].default("http://0.0.0.0"),
      env("LOG_APP").as[Boolean].default(false),
      env("LOG_CLI").as[Boolean].default(false)).parMapN{(ghuri, token, url, app, cli) =>
      ServerConfig(
        app,
        cli,
        GitHubApiConfig(ghuri, token, url))
    }

  def stream[F[_]: ConcurrentEffect : Logger](conf: ServerConfig)(implicit T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {


    for {
      client <- BlazeClientBuilder[F](global).stream
      loggedClient = if(conf.clientLogging){ org.http4s.client.middleware.Logger(true, true, _ => false)(client)} else client
      api: GitHubApiAlgebra[F] = githubapiinterpreter.create(loggedClient, conf.gitHubApiConfig)
      db: GitHubPersistentStoreAlgebra[F] = doobiepersisteninterpreter.DoobiePersistentStore(Database.xa)
      service = repositoryserviceinterpreter.RepositoryServiceInterpreter(api,db)

      httpApp = (
        Routes.contributorRoutes[F](service)
      ).orNotFound

      loggedHttpApp = if(conf.appLogging){middleware.Logger.httpApp(true, true)(httpApp)} else httpApp

      exitCode <- BlazeServerBuilder[F]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(loggedHttpApp)
        .serve
    } yield exitCode
  }.drain
}