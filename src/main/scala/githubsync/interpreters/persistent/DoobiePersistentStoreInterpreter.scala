package githubsync.interpreters.persistent

import cats.effect.Sync
import doobie.Transactor
import githubsync.domain.GitHubApi.{GitHubApiAlgebra, GitHubPersistentStoreAlgebra}
import githubsync.domain.{Repository, User}
import githubsync.interpreters.upstream.GitHubApiInterpreter.GitHubApiConfig
import org.http4s.client.Client
import doobie._
import doobie.implicits._
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import fs2.Stream

object DoobiePersistentStoreInterpreter {

  def create[F[_] : Sync](xa: Transactor[F]): GitHubPersistentStoreAlgebra[F] =
    new GitHubPersistentStoreAlgebra[F] {
      def addRepositories[C[_] : Foldable : Monad](c: C[Repository]): F[Int] = {
        val insert = "insert into repositories (name, owner) values (?, ?)"
        val update: doobie.ConnectionIO[Int] = Update[(String, String)](insert).updateMany(c.map(r => (r.name, r.owner)))
        update.transact(xa)
      }

      //! registered name, initialized = false
      def register(org: String): F[Int] = {
        (sql"insert into registered (name) values ($org)")
          .update
          .run
          .transact(xa)
      }

      //def initialize(org)
      //! registered set initinialized to true

      def registered(org: String): Stream[F, String] = {
        sql"select name from registered where name=$org"
          .query[String]
          .stream
          .transact(xa)
      }

      def repositories(org: String): Stream[F, Repository] = {
        sql"select name,owner from repositories where owner='$org'"
          .query[Repository]
          .stream
          .transact(xa)
      }

      def stargazers(repo: Repository): Stream[F, User] = {
        sql"select name,repository from stargazers where repository='${repo.name}'"
          .query[User]
          .stream
          .transact(xa)
      }

      def addStarGazers[C[_] : Foldable : Monad](c: C[User]): F[Int] = {
        val insert = "insert into stargazers (name, repo) values (?, ?)"
        val update: doobie.ConnectionIO[Int] = Update[(String, String)](insert).updateMany(c.map(r => (r.name, r.repo)))
        update.transact(xa)
      }


    }


}
