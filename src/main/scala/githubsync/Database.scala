package githubsync

import cats.effect.{Async, Blocker, ContextShift, IO, Resource}
import doobie.util.transactor.Transactor
import doobie.util.ExecutionContexts

import scala.concurrent.ExecutionContext

object Database {
  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

  def xa[F[_]: Async: ContextShift] = Transactor.fromDriverManager[F](
    "org.postgresql.Driver",
    "jdbc:postgresql://localhost:5432/",
    "postgres",
    "password",
    Blocker.liftExecutionContext(ExecutionContexts.synchronous)
  )


}
