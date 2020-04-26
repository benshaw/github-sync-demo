package githubsync.interpreters.service

import cats.{Monad, MonadError}
import cats.effect.{Concurrent, Sync}
import githubsync.algebras.github.{GitHubApiAlgebra, GitHubPersistentStoreAlgebra}
import githubsync.algebras.service.{EventService, RepositoryService, UnknownAction}
import githubsync.domain.repositoryevent.RepositoryEvent
import githubsync.domain.starevent.StarEvent
import githubsync.domain.user.User
import cats.implicits._
import fs2.Stream
import githubsync.domain.repository.Repository

class eventserviceinterpreter {

  def EventServiceInterpreter[F[_]](api: GitHubApiAlgebra[F], persistentStorage: GitHubPersistentStoreAlgebra[F])(implicit F: Sync[F]) =
    new EventService[F] {

      def starEvent(e: StarEvent): F[Int] = {
        val u = User(name = e.sender.login, repo = e.repository.name)
        e.action match {
          case "created" => persistentStorage.addStarGazers(List(u))
          case "deleted" => persistentStorage.deleteStarGazer(u)
          case e => F.raiseError(UnknownAction(e))
        }
      }

      def repoEvent(e: RepositoryEvent) :F[Int]= {
        val r = Repository(e.repository.name, e.repository.owner.login)
        e.action match {
          case "created" => addRepo(r).compile.toList.map(l => l.size)
          case "deleted" => persistentStorage.deleteRepository(r)
          case "archived" => F.pure(0)
          case "edited" => F.pure(0)
          case "renamed" => F.pure(0)
          case "transferred" => F.pure(0)
          case "publicized" => F.pure(0)
          case "privatized" => F.pure(0)
          case e => F.raiseError(UnknownAction(e))
        }
      }

      private def addRepo(r: Repository): Stream[F, Int] = {
        api.stargazers(r).chunkN(10).flatMap(o => Stream.eval(persistentStorage.addStarGazers(o)))
      }
    }
}
