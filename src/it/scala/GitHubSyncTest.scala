package githubsync.interpreters

import githubsync.TestData
import cats.effect.IO
import cats.effect._
import io.circe.Json
import org.http4s.{Header, Headers, Response}
import org.http4s.client.Client
import org.http4s.dsl.io._
import io.circe.syntax._
import org.http4s.circe._
import eu.timepit.refined.auto._
import fs2.Pure
import githubsync.interpreters.upstream.githubapiinterpreter
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.generic.auto._

//! \todo get case class matching working
//import scala.language.experimental.macros
//import scala.language.existentials

class GitHubApiInterpreterTest extends org.specs2.mutable.Specification with org.specs2.matcher.MatcherMacros {

  import TestData._
  import githubsync.interpreters.upstream.githubapiinterpreter._

  private[this] val config = GitHubApiConfig("https://www.test.com", None, "https://www.test.com")

  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  "Event Service" >> {

    /*
     val cont: RepositoryService[IO] = repositoryserviceinterpreter.RepositoryServiceInterpreter[IO](errorApi)
      val req = Request[IO](Method.GET, uri"/org/someorg/contributors")
      val r = Routes.contributorRoutes(cont).orNotFound(req).unsafeRunSync()
      r.status must beEqualTo(Status.InternalServerError)
     */

    "Deletes Stars" >> {

    }

    "Add Stars" >> {

    }

    "Adds Repos" >> {

    }

    "Delete repos" >>{

    }
  }

}
