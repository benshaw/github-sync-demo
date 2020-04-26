package githubsync.interpreters.service

import cats.effect.{Concurrent, Sync}
import githubsync.algebras.github.{GitHubApiAlgebra, GitHubPersistentStoreAlgebra}
import githubsync.algebras.service.{EventDownStreamError, EventError, EventService, RepositoryService, UnknownAction}
import githubsync.domain.repositoryevent.RepositoryEvent
import githubsync.domain.starevent.StarEvent
import githubsync.domain.user.User
import cats._
import cats.implicits._
import fs2.Stream
import githubsync.domain.repository.Repository
import io.chrisdavenport.log4cats.Logger

object eventserviceinterpreter {


  def EventServiceInterpreter[F[_]: Logger](api: GitHubApiAlgebra[F], persistentStorage: GitHubPersistentStoreAlgebra[F])(implicit F: Sync[F], E: MonadError[F, Throwable]) =
    new EventService[F] {

      implicit private final class EventServiceErrorOps[A](private val fa: F[A]) {
        def downStreamError(): F[A] =
          fa.adaptError {
            case e => EventDownStreamError(e)
          }
      }

      def starEvent(e: StarEvent): F[Int] = {
        val u = User(name = e.sender.login, repo = e.repository.name)

        e.action match {
          case "created" => Logger[F].info(s"Adding new star gazer") *> persistentStorage.addStarGazers(List(u)).downStreamError()
          case "deleted" => Logger[F].info(s"Deleting star gazer") *> persistentStorage.deleteStarGazer(u).downStreamError()
          case e => F.raiseError(UnknownAction(e))
        }

      }

      def repoEvent(e: RepositoryEvent) :F[Int]= {
        val r = Repository(e.repository.name, e.repository.owner.login)
        e.action match {
          case "created" => Logger[F].info(s"Adding Repository") *> addRepo(r).downStreamError()
          case "deleted" => Logger[F].info(s"Deleting Repository") *> persistentStorage.deleteRepository(r).downStreamError()
          case "archived" => F.pure(0)
          case "edited" => F.pure(0)
          case "renamed" => F.pure(0)
          case "transferred" => F.pure(0)
          case "publicized" => F.pure(0)
          case "privatized" => F.pure(0)
          case e => F.raiseError(UnknownAction(e))
        }

      }

      private def addRepo(r: Repository): F[Int] = {
        api
          .stargazers(r)
          .chunkN(10)
          .flatMap(o => Stream.eval(persistentStorage.addStarGazers(o)))
          .compile
          .toList
          .map(l => l.size)
      }
    }
}
