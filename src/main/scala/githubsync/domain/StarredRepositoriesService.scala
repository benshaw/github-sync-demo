package githubsync.domain

import GitHubApi.{ErrorRetrievingJson, GitHubApiAlgebra, GitHubApiError, ResourceNotFound}
import cats.{Monad, MonadError}
import cats.data.Kleisli
import cats.effect.Sync
import cats.implicits._

abstract class StarredRepositoriesService[F[_]](github: GitHubApiAlgebra[F]) {
  def get: Kleisli[F, String, List[Repository]]
  //def add: Kleisli[F, String, List[Repository]]
}

sealed trait ContributorError extends Exception

object StarredRepositoriesService {

  case class DownStreamError(e: String) extends ContributorError

  case class NotFound(e: String) extends ContributorError

  def create[F[_]](github: GitHubApiAlgebra[F])(implicit F: Monad[F], E: MonadError[F, Throwable]): StarredRepositoriesService[F] = new StarredRepositoriesService(github) {

    private val getRepositories: Kleisli[F, String, List[Repository]] = Kleisli((orgName: String) =>
      github
        .repositories(orgName)
        /*.adaptError {
        //! \todo LOG
          case e => DownStreamError(e.toString)
        }*/
    )

    private val getContributors = (r: Repository) =>
      github.contributors(r)
        .recover {
          case ResourceNotFound(_) => List.empty
        }
        /*
        .adaptError {
          case e => DownStreamError(e.toString)
        }*/
    
    private val getStarred = (r: User) =>
      github.starred(r)
        /*.recover {
          case ResourceNotFound(_) => List.empty
        }
        .adaptError {
          case e => DownStreamError(e.toString)
        }*/
    
    private val getRepositoryContributors: Kleisli[F, List[Repository], List[User]] = Kleisli((repo: List[Repository]) =>
      repo
        .flatTraverse(getContributors)
        .map(sortContributorList)
    )
    
    private val getUsersStarredRepositories: Kleisli[F, List[User], List[Repository]] = Kleisli((users: List[User]) =>
      users
          .filterNot(_.name.contains("[bot]"))
          .flatTraverse(getStarred)
      //repo
        //.flatTraverse(getContributors)
       // .map(sortContributorList)
    )

    private val sortContributorList = (c: List[User]) =>
      c.groupBy(_.name)
        .map(c => User(c._1, c._2.map(_.contributions).sum))
        .toList
        .sortBy(_.contributions)(Ordering.Int.reverse)

    override val get: Kleisli[F, String, List[Repository]] =
      getRepositories andThen getRepositoryContributors andThen getUsersStarredRepositories

  }
}