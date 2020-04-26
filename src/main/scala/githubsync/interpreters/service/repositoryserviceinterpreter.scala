package githubsync.interpreters.service

import cats.effect.Concurrent
import cats.effect.implicits._
import cats.implicits._
import cats.{Monad, MonadError}
import fs2.Stream
import fs2._
import githubsync.algebras.github.{GitHubApiAlgebra, GitHubPersistentStoreAlgebra}
import githubsync.algebras.service.RepositoryService
import githubsync.domain.user._
import githubsync.domain.repository._


object repositoryserviceinterpreter {

  //! \todo rename
  sealed trait Error extends Exception


  //case class DownStreamError(e: String) extends ContributorError

  //case class NotFound(e: String) extends ContributorError

  def RepositoryServiceInterpreter[F[_] : Concurrent](api: GitHubApiAlgebra[F], persistentStorage: GitHubPersistentStoreAlgebra[F])(implicit F: Monad[F], D: MonadError[F, Throwable]): RepositoryService[F] =

    new RepositoryService[F] {

      def stargazers(org: String): Stream[F, Option[User]] =
        isInitialized(org).flatMap {
          case Some(_) => getFromDb(org)
          case None => InitializeAsync(org)
        }

      private def InitializeAsync(org: String): Stream[F, Option[User]] =
        Stream.eval(initialize(org).compile.toList.start.void).map(_ => None)

      //! \todo cleanup
      private def isInitialized(org: String): Stream[F, Option[String]] =
        Stream.eval(persistentStorage.registered(org).head.compile.toList.map(_.headOption))

      private def getFromDb(org: String): Stream[F, Option[User]] = for {
        repos <- persistentStorage.repositories(org)
        gazers <- persistentStorage.stargazers(repos)
      } yield Some(gazers)

      private def initialize(org: String): Stream[F, Option[User]] = for {
        _ <- syncDbAndRegisterHooks(org)
        _ <- Stream.eval(persistentStorage.register(org))
      } yield None

      private def syncDbAndRegisterHooks(org: String): Stream[F, Int] ={
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