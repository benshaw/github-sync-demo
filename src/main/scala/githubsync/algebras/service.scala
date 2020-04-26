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

  trait ServiceError extends Throwable
  case class UnknownAction(a: String) extends ServiceError
}

