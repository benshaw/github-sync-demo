package githubsync

import cats.effect.{Async, Blocker, ContextShift, IO, Resource}
import doobie.util.transactor.Transactor
import doobie.util.ExecutionContexts

import scala.concurrent.ExecutionContext

object Database {
  /*
  case class DatabaseConfig(driver: String,
                             url:    String,
                             user:   String,
                             pass:   String)


  //! \todo temp
  val defaultConfig = DatabaseConfig(
    driver="org.h2.Driver",
    url="jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
    user="sa",
    pass="")

  def dbTransactor[F[_]: Async: ContextShift](config: DatabaseConfig,
                                               //ec: ExecutionContext,
                                               blocker: Blocker) =

    Transactor.fromDriverManager[F](config.driver,
      config.url,
      config.user,
      config.pass,
      blocker)

   */

  // We need a ContextShift[IO] before we can construct a Transactor[IO]. The passed ExecutionContext
  // is where nonblocking operations will be executed. For testing here we're using a synchronous EC.
  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

  // A transactor that gets connections from java.sql.DriverManager and executes blocking operations
  // on an our synchronous EC. See the chapter on connection handling for more info.
  def xa[F[_]: Async: ContextShift] = Transactor.fromDriverManager[F](
    "org.postgresql.Driver",     // driver classname
    "jdbc:postgresql://localhost:5432/",     // connect URL (driver-specific)
    "postgres",                  // user
    "password",                          // password
    Blocker.liftExecutionContext(ExecutionContexts.synchronous) // just for testing
  )

  /*
  // Resource yielding a transactor configured with a bounded connect EC and an unbounded
  // transaction EC. Everything will be closed and shut down cleanly after use.
  def transactor()(implicit c: ContextShift[IO]): Resource[IO, H2Transactor[IO]] =
  for {
    ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
    be <- Blocker[IO]    // our blocking EC
    xa <- H2Transactor.newH2Transactor[IO](
      "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", // connect URL
      "sa",                                   // username
      "",                                     // password
      ce,                                     // await connection here
      be                                      // execute JDBC operations here
    )
  } yield xa*/

}
