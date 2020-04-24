package githubsync.domain

import GitHubApi.{ErrorRetrievingJson, GitHubApiAlgebra, GitHubApiError, ResourceNotFound}
import cats.{Monad, MonadError}
import cats.data.Kleisli
import cats.effect.Sync
import cats.implicits._
import fs2.Stream

abstract class RepositoryService[F[_]](github: GitHubApiAlgebra[F]) {
  def get(org:String): Stream[F, Repository]
  //def getStarred
  //def addStar: Kleisli[F, String, List[Repository]]
}

sealed trait ContributorError extends Exception

object RepositoryService {

  case class DownStreamError(e: String) extends ContributorError

  case class NotFound(e: String) extends ContributorError

  def create[F[_]](api: GitHubApiAlgebra[F])(implicit F: Monad[F], E: MonadError[F, Throwable]): RepositoryService[F] = new RepositoryService(api) {

    def get(org:String): Stream[F, Repository] =
      for {
        repo <- api.repositories(org)
        cont <- api.contributors(repo).filterNot(_.name.contains("[bot]"))
        starred <- api.starred(cont)
      } yield repo


    /*
    private val getRepositories = Kleisli((orgName: String) =>
      api
        .repositories(orgName)
        /*.adaptError {
        //! \todo LOG
          case e => DownStreamError(e.toString)
        }*/
    )

    private val getContributors = (r: Repository) =>
      api.contributors(r)
        .recover {
          case ResourceNotFound(_) => List.empty
        }
        /*
        .adaptError {
          case e => DownStreamError(e.toString)
        }*/

    private val getStarred = (r: User) =>
      api.starred(r)
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
    
    private val getUsersStarredRepositories: Kleisli[Nothing, Any, Nothing] = Kleisli((users: Stream[User]) =>
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

*/
  }
}