package githubsync.domain

import GitHubApi.{ErrorRetrievingJson, GitHubApiAlgebra, GitHubApiError, GitHubPersistentStoreAlgebra, ResourceNotFound}
import cats.{Monad, MonadError}
import cats.data.Kleisli
import cats.effect.{Async, Concurrent, Sync}
import cats.implicits._
import fs2.{INothing, Stream}


abstract class RepositoryService[F[_]](github: GitHubApiAlgebra[F]) {
  def stargazers(org:String): Stream[F, Option[User]]
  //def addStar()
  //def addRepository()
}


object RepositoryService {

  //! \todo rename
  sealed trait Error extends Exception

  case class OrgNotAdded(org: String) extends Error
  case class DbError1(err: Throwable) extends Error
  case class DbError2(err: Throwable) extends Error
  case class DbError3(err: Throwable) extends Error
  case class DbError4(err: Throwable) extends Error
  case class DbError5(err: Throwable) extends Error

  //case class DownStreamError(e: String) extends ContributorError

  //case class NotFound(e: String) extends ContributorError

  def create[F[_] : Concurrent](api: GitHubApiAlgebra[F], persistantStorage: GitHubPersistentStoreAlgebra[F])(implicit F: Monad[F], E: MonadError[F, Throwable]): RepositoryService[F] = new RepositoryService(api) {

    private def registered(org: String): Stream[F, Option[String]] =
      Stream.eval(persistantStorage.registered(org).adaptError{
        e => DbError1(e)
      }.take(1).compile.toList.map(_.headOption))

    //! \todo could this be optimized ?
    //! \have to wait till webhook are combined to be sure
    def getFromDb(org: String): Stream[F, Option[User]] = for {
      repos <-persistantStorage.repositories(org).adaptError{
        e => DbError2(e)
      }
      gazers <- persistantStorage.stargazers(repos).adaptError{
        e => DbError3(e)
      }
    } yield Some(gazers)

    def populateDb(org: String): Stream[F, Int] = {
      val repos = api.repositories(org)
      val gazers = repos.flatMap(r => api.stargazers(r))

      val addGazers = gazers.chunkN(10).flatMap(o => Stream.eval(persistantStorage.addStarGazers(o).adaptError{
        e => DbError4(e)
      }))

      val addRepos = repos.chunkN(10).flatMap(o => Stream.eval(persistantStorage.addRepositories(o).adaptError{
        e => DbError5(e)
      }))

      addGazers.concurrently(addRepos)
    }

    def register(org: String): Stream[F, Option[User]] = {

      populateDb(org).map(_ => None)
      //Stream.empty.map(_ => None).concurrently(populateDb(org))
    //}
     // Async[F].async { cb =>

        //F.raiseError

        //! register

        //! Add to registered db

        //! add webhooks

        //! async populate db
        //! \todo async


        //.chunkN(10) //\todo config
        //.flatMap(o => Stream.eval(persistantStorage.addRepositories(o)))
        //.evalMap(c => persistantStorage.addRepositories(c))
      }


      def stargazers(org: String): Stream[F, Option[User]] = {

        /*
        val repo: Stream[F, Repository] = for {
          repo <- api.repositories(org)

          //cont <- api.contributors(repo).filterNot(_.name.contains("[bot]"))
          //starred <- api.starred(cont)
        } yield repo
  */
        registered(org).flatMap{
          case Some(_) => getFromDb(org)
          case None => register(org)
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