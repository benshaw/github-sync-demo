package githubsync.interpreters.persistent

import java.sql.SQLException

import cats.effect.Sync
import doobie.Transactor
import githubsync.algebras.github.{GitHubApiAlgebra, GitHubPersistentStoreAlgebra, InsertionError, SelectionError}
import io.chrisdavenport.log4cats.Logger
import githubsync.interpreters.upstream.githubapiinterpreter.GitHubApiConfig
import org.http4s.client.Client
import fs2.Stream
import doobie._
import doobie.implicits._
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import githubsync.domain.repository._
import githubsync.domain.user._

object doobiepersisteninterpreter {

  def logHandler = LogHandler.jdkLogHandler

  def DoobiePersistentStore[F[_] : Sync: Logger](xa: Transactor[F]): GitHubPersistentStoreAlgebra[F] =
    new GitHubPersistentStoreAlgebra[F] {

      def deleteStarGazer(u: User):F[Int] =
        sql"delete from stargazers where repo=${u.repo} and name=${u.name}"
          .updateWithLogHandler(logHandler)
          .run
          .transact(xa)
          .adaptError {
            case e => InsertionError(e)
          }
          .onError {
            case e =>
              Logger[F].error(s"Failed Delete from stargazers $u got error ${e.getMessage}")
          }

      //! Delete repository and stargazers for that repo
      def deleteRepository(r: Repository): F[Int]  = {

        val deleteRepo: F[Int] =
          sql"delete from repositories where name=${r.name} and owner=${r.owner}"
          .updateWithLogHandler(logHandler)
          .run
          .transact(xa)
          .adaptError {
            case e => InsertionError(e)
          }
          .onError {
            case e =>
              Logger[F].error(s"Failed Delete $r from repositories got error ${e.getMessage}")
          }

        val deleteGazers: F[Int] =
        sql"delete from stargazers where repo=${r.name}"
          .updateWithLogHandler(logHandler)
          .run
          .transact(xa)
          .adaptError {
            case e => InsertionError(e)
          }
          .onError {
            case e =>
              Logger[F].error(s"Failed Delete from stargazers where repo=${r.name} got error ${e.getMessage}")
          }

        for {
          g <- deleteGazers
          r <- deleteRepo
        } yield g+r

      }

      def addRepositories[C[_] : Foldable : Monad](c: C[Repository]): F[Int] = {
        val insert = "insert into repositories (name, owner) values (?, ?)"
        val update: doobie.ConnectionIO[Int] = Update[(String, String)](insert,None,logHandler).updateMany(c.map(r => (r.name, r.owner)))
        update
          .transact(xa)
          .adaptError {
            case e => InsertionError(RepositoryError(e))
          }
          .onError {
            case e =>
              Logger[F].error(s"Failed to insert repositories received error ${e.getMessage}")
          }
      }

      //! registered name, initialized = false
      def register(org: String): F[Int] = {
        (sql"insert into registered (name) values ($org)")
          .updateWithLogHandler(logHandler)
          .run
          .transact(xa)
          .adaptError {
            case e => InsertionError(e)
          }
          .onError {
            case e =>
              Logger[F].error(s"Failed to register $org got error ${e.getMessage}")
          }
      }

      //def initialize(org)
      //! registered set initinialized to true

      def registered(org: String): Stream[F, String] = {
        sql"select name from registered where name=$org"
          .query[String]
          .stream
          .transact(xa)
          .adaptError {
            case e => SelectionError(e)
          }
          .onError {
            case e =>
              Stream.eval(Logger[F].error(s"Failed to select registered for $org received error ${e.getMessage}"))
          }
      }

      def repositories(org: String): Stream[F, Repository] = {
        sql"select name,owner from repositories where owner=$org"
          .queryWithLogHandler[Repository](logHandler)
          .stream
          .transact(xa)
          .adaptError {
            case e => SelectionError(RepositoryError(e))
          }
          .onError {
            case e =>
              Stream.eval(Logger[F].error(s"Failed to select from repositories for $org received error ${e.getMessage}"))
          }
      }

      def stargazers(repo: Repository): Stream[F, User] = {
        sql"select name,repo from stargazers where repo=${repo.name}"
          .queryWithLogHandler[User](logHandler)
          .stream
          .transact(xa)
          .adaptError {
            case e => SelectionError(UserError(e))
          }
          .onError {
            case e =>
              Stream.eval(Logger[F].error(s"Failed to select from stargazers for ${repo.name} received error ${e.getMessage}"))
          }


      }

      def addStarGazers[C[_] : Foldable : Monad](c: C[User]): F[Int] = {
        val insert = "insert into stargazers (name, repo) values (?, ?)"
        val update: doobie.ConnectionIO[Int] = Update[(String, String)](insert, None, logHandler).updateMany(c.map(r => (r.name, r.repo)))
        update
          .transact(xa)
          .adaptError {
            case e => InsertionError(UserError(e))
          }
          .onError {
            case e =>
              Logger[F].error(s"Failed to insert into stargazers received error ${e.getMessage}")
          }
      }


    }


}
