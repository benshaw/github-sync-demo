package githubsync.interpreters.persistent

import cats.effect.Sync
import doobie.Transactor
import githubsync.domain.GitHubApi.{GitHubApiAlgebra, GitHubPersistentStoreAlgebra}
import githubsync.domain.Repository
import githubsync.interpreters.upstream.GitHubApiInterpreter.GitHubApiConfig
import org.http4s.client.Client
import doobie._
import doobie.implicits._
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import fs2.Stream

object GitHubPersistentStoreInterpreter {


  def create[F[_] : Sync](xa: Transactor[F]): GitHubPersistentStoreAlgebra[F] =

    new GitHubPersistentStoreAlgebra[F] {

      type repo = (String, String)

      def addRepositories[C[_]: Foldable : Monad](c: C[Repository]): F[Int] = {
        val insert =  "insert into repositories (name, owner) values (?, ?)"
         val update: doobie.ConnectionIO[Int] = Update[(String,String)](insert).updateMany(c.map(r => (r.name, r.owner)))
        update.transact(xa)
      }

      def repositories(org: String): Stream[F, Repository] = {
        Stream.empty
        /*
        sql"select * from repositories where owner='$org'"
          .query[Repository]    // Query0[String]
          .stream           // Stream[ConnectionIO, String]
          .transact(xa)*/
      }
    }



}
