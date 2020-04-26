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
import githubsync.interpreters.upstream.githubapiinterpreter

//! \todo get case class matching working
//import scala.language.experimental.macros
//import scala.language.existentials

class GitHubApiInterpreterTest extends org.specs2.mutable.Specification with org.specs2.matcher.MatcherMacros {

  import TestData._
  import githubsync.interpreters.upstream.githubapiinterpreter._

  "GitHubApiInterpreter" >> {

    "Handle Upstream Errors" >> {
      val err = "Error"
      val resp: IO[Response[IO]] = BadRequest(err)
      val client: Client[IO] =
        Client[IO]({ req => Resource
          .liftF(resp)
        })

      val g = githubapiinterpreter.create[IO](client, config)
      //! \todo match left case class
      //g.repositories("x").attempt.unsafeRunSync() must beLeft((i:GitHubError) => i must matchA[ResourceNotFound])
      g.repositories("x").attempt.unsafeRunSync() must beLeft
    }

    "Handle Invalid Json Errors" >> {
      val invalidJson = "Some Invalid Json"
      val resp: IO[Response[IO]] = Ok(invalidJson)
      val client: Client[IO] =
        Client[IO]({ req => Resource
          .liftF(resp)
        })

      val g = githubapiinterpreter.create[IO](client, config)
      //! \todo match case class (parse error)
      g.repositories("x").attempt.unsafeRunSync() must beLeft
    }

    "GetContributors" >> {
      val resp: IO[Response[IO]] = Ok(IO.pure(gitHubCont.asJson))
      val client: Client[IO] =
        Client[IO]({ req => Resource
          .liftF(resp)
        })

      val g = githubapiinterpreter.create[IO](client, config)

      g.contributors(repos.head).unsafeRunSync() must containTheSameElementsAs(cont)
    }

    "GetRepositories" >> {
      val resp: IO[Response[IO]] = Ok(IO.pure(gitHubRepo.asJson))
      val client: Client[IO] =
        Client[IO]({ req => Resource
          .liftF(resp)
        })

      val g = githubapiinterpreter.create[IO](client, config)

      g.repositories("x").unsafeRunSync() must containTheSameElementsAs(repos)
    }

    "Pagination two page" >>{
      val d: Json = gitHubRepo.asJson
      val resp: IO[Response[IO]] =
        Ok(d,
          Header("Link", "<https://api.github.com/search/code?q=addClass+user%3Amozilla&page=2>; rel=\"next\""),
          Header("Link", "<https://api.github.com/search/code?q=addClass+user%3Amozilla&page=4>; rel=\"last\""))//IO(Response(status=200, headers=Headers(Content-Length: 0)))
      val client: Client[IO] =
        Client[IO]({ req => Resource
          .liftF(resp)
        })

      val g = githubapiinterpreter.create[IO](client, config)

      g.repositories("x").unsafeRunSync().map(_.name) must containTheSameElementsAs((repos ::: repos ::: repos ::: repos).map(_.name))
    }
  }

  private[this] val config = GitHubApiConfig("https://www.test.com", None)
}
