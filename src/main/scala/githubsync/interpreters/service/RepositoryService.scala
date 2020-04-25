package githubsync.interpreters.service

import cats.effect.Concurrent
import cats.effect.implicits._
import cats.implicits._
import cats.{Monad, MonadError}
import fs2.Stream
import githubsync.algebras.GitHubApi.{GitHubApiAlgebra, GitHubPersistentStoreAlgebra}
import githubsync.domain.user._
import githubsync.domain.repository._


abstract class RepositoryService[F[_]](github: GitHubApiAlgebra[F]) {
  def stargazers(org: String): Stream[F, Option[User]]

  //def addStar()
  //def addRepository()
}


object RepositoryService {

  //! \todo rename
  sealed trait Error extends Exception


  //case class DownStreamError(e: String) extends ContributorError

  //case class NotFound(e: String) extends ContributorError

  def create[F[_] : Concurrent](api: GitHubApiAlgebra[F], persistantStorage: GitHubPersistentStoreAlgebra[F])(implicit F: Monad[F],D: MonadError[F, Throwable]): RepositoryService[F] = new RepositoryService(api) {

    //! \todo cleanup
    private def isInitialized(org: String): Stream[F, Option[String]] =
      Stream.eval(persistantStorage.registered(org).head.compile.toList.map(_.headOption))

    //! \todo error reporting
    //! \todo could this be optimized ?
    //! \have to wait till webhook are combined to be sure
    def getFromDb(org: String): Stream[F, Option[User]] = for {
      repos <- persistantStorage.repositories(org)
      gazers <- persistantStorage.stargazers(repos)
    } yield Some(gazers)

    def initialize(org: String): F[List[Int]] = {
      val repos = api.repositories(org)
      val gazers = repos.flatMap(r => api.stargazers(r))

      val addGazers = gazers.chunkN(10).flatMap(o => Stream.eval(persistantStorage.addStarGazers(o)))

      val addRepos = repos.chunkN(10).flatMap(o => Stream.eval(persistantStorage.addRepositories(o)))

      addGazers.concurrently(addRepos).concurrently(Stream.eval(persistantStorage.register(org))).compile.toList
    }



    def stargazers(org: String): Stream[F, Option[User]] = {

      isInitialized(org).flatMap {
        case Some(_) => getFromDb(org)
        case None => Stream.eval(initialize(org).start.void.map(_ => None))
      }

    }


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