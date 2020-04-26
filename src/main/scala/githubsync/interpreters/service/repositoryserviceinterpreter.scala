package githubsync.interpreters.service

import cats.effect.{Concurrent, Sync}
import cats.{Monad, MonadError}
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import githubsync.algebras.github._
import githubsync.algebras.service._
import cats.effect.implicits._
import cats.implicits._
import githubsync.domain.user._
import githubsync.domain.repository._

object repositoryserviceinterpreter {

  def RepositoryServiceInterpreter[F[_] : Concurrent : Logger: Sync](api: GitHubApiAlgebra[F], persistentStorage: GitHubPersistentStoreAlgebra[F])(implicit F: Monad[F]): RepositoryService[F] =
    new RepositoryService[F] {

      implicit private final class RepositoryServiceErrorOps[A](private val fa: Stream[F, A]) {
        def downStreamError(): Stream[F, A] =
          fa.adaptErr {
            case UriParseError(e) => DownStreamBadRequest(e, "Error Parsing request URL")
            case ResourceNotFound(e) => DownStreamBadRequest(e, "Unable to find the resource on the downstream API")
            case e => RepositoryDownStreamError(e)
          }
      }

      def stargazers(org: String): Stream[F, Option[User]] =
        isInitialized(org).flatMap {
          case Some(_) => getFromDb(org)
          case None => InitializeAsync(org)
        }.downStreamError()

      private def InitializeAsync(org: String): Stream[F, Option[User]] =
        Stream.eval(
          for {
            _ <- Logger[F].info(s"Initializing Data for $org Asynchronously")
            r <- initialize(org).compile.toList.start.void
          } yield r
        ).map(_ => None)

      private def isInitialized(org: String): Stream[F, Option[String]] =
        Stream.eval(persistentStorage.registered(org).head.compile.toList.map(_.headOption))

      private def getFromDb(org: String): Stream[F, Option[User]] = for {
        _ <- Stream.eval(Logger[F].info(s"Retrieving data from persistent storage for $org"))
        repos <- persistentStorage.repositories(org)
        gazers <- persistentStorage.stargazers(repos)
      } yield Some(gazers)

      private def initialize(org: String): Stream[F, Option[User]] = for {
        _ <- syncDbAndRegisterHooks(org)
        _ <- Stream.eval(persistentStorage.register(org))
      } yield None

      private def syncDbAndRegisterHooks(org: String): Stream[F, Int] = {
        val repos: Stream[F, Repository] =
          api.repositories(org)

        val gazers: Stream[F, User] =
          repos.flatMap(r => api.stargazers(r))

        val addGazers: Stream[F, Int] =
          gazers.chunkN(10).flatMap(o => Stream.eval(persistentStorage.addStarGazers(o)))

        val addRepos: Stream[F, Int] =
          repos.chunkN(10).flatMap(o => Stream.eval(persistentStorage.addRepositories(o)))

        val syncData: Stream[F, Int] =
          addGazers.concurrently(addRepos)

        val registerWebHooks: Stream[F, Unit] = for {
          _ <- repos.map(api.registerForRepoEvents)
          _ <- repos.map(api.registerForStarEvents)
        } yield ()

        syncData.concurrently(registerWebHooks)

      }
    }
}