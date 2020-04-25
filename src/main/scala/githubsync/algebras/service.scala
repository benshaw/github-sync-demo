package githubsync.algebras

import fs2.Stream
import githubsync.algebras.github.GitHubApiAlgebra
import githubsync.domain.user.User

object service {

  trait RepositoryService[F[_]] {
    def stargazers(org: String): Stream[F, Option[User]]

    def addStar()

    def addRepository()
  }

}

