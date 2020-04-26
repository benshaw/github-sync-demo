package githubsync.algebras

import fs2.Stream
import githubsync.domain.repositoryevent.RepositoryEvent
import githubsync.domain.starevent.StarEvent
import githubsync.domain.user.User

object service {

  trait RepositoryService[F[_]] {
    def stargazers(org: String): Stream[F, Option[User]]
  }

  trait EventService[F[_]]{
    def starEvent(e: StarEvent): F[Int]
    def repoEvent(e: RepositoryEvent): F[Int]
  }

  trait RepositoryError extends Exception
  case class RepositoryDownStreamError(e : Throwable) extends RepositoryError
  case class DownStreamBadRequest(e: String, msg: String) extends RepositoryError

  trait EventError extends Throwable
  case class EventDownStreamError(e : Throwable) extends EventError
  case class UnknownAction(a: String) extends EventError
}

