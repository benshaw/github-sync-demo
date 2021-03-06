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

  "GitHubApiInterpreter" >> {

    "Handle Upstream Errors" >> {
      val err = "Error"
      val resp: IO[Response[IO]] = BadRequest(err)
      val client: Client[IO] =
        Client[IO]({ req => Resource
          .liftF(resp)
        })

      val g = githubapiinterpreter.GitHubApiInterpreter[IO](client, config)
      //! \todo match left case class
      //g.repositories("x").attempt.unsafeRunSync() must beLeft((i:GitHubError) => i must matchA[ResourceNotFound])
      g.repositories("x").compile.toList.attempt.unsafeRunSync() must beLeft
    }

    "Handle Invalid Json Errors" >> {
      val invalidJson = "Some Invalid Json"
      val resp: IO[Response[IO]] = Ok(invalidJson)
      val client: Client[IO] =
        Client[IO]({ req => Resource
          .liftF(resp)
        })

      val g = githubapiinterpreter.GitHubApiInterpreter[IO](client, config)
      //! \todo match case class (parse error)
      g.repositories("x").compile.toList.attempt.unsafeRunSync() must beLeft
    }

    "Repositories" >> {
      val resp: fs2.Stream[IO, Json] = gitHubRepo.map(_.asJson)
      val client: Client[IO] =
        Client[IO]({ req => Resource
          .liftF(Ok(resp))
        })

      val g = githubapiinterpreter.GitHubApiInterpreter[IO](client, config)

      g.repositories(owner).compile.toList.unsafeRunSync() must containTheSameElementsAs(repos.compile.toList)
    }

    "Stars" >> {
      val resp: fs2.Stream[IO, Json] = gitHubStars.map(_.asJson)
      val client: Client[IO] =
        Client[IO]({ req => Resource
          .liftF(Ok(resp))
        })

      val g = githubapiinterpreter.GitHubApiInterpreter[IO](client, config)

       g.stargazers(repo1).compile.toList.unsafeRunSync() must containTheSameElementsAs(starGazersR1.compile.toList)
    }

    "Pagination two page" >>{
      val d: fs2.Stream[IO, Json] = gitHubRepo.map(_.asJson)
      val resp: IO[Response[IO]] =
        Ok(d,
          Header("Link", "<https://api.github.com/search/code?q=addClass+user%3Amozilla&page=2>; rel=\"next\""),
          Header("Link", "<https://api.github.com/search/code?q=addClass+user%3Amozilla&page=4>; rel=\"last\""))//IO(Response(status=200, headers=Headers(Content-Length: 0)))
      val client: Client[IO] =
        Client[IO]({ req => Resource
          .liftF(resp)
        })

      val g = githubapiinterpreter.GitHubApiInterpreter[IO](client, config)

      g.repositories("x").compile.toList.unsafeRunSync().map(_.name) must containTheSameElementsAs((repos.compile.toList ::: repos.compile.toList ::: repos.compile.toList ::: repos.compile.toList).map(_.name))
    }
  }

}
