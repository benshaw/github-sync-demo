package githubsync
import githubsync.TestData._
import githubsync.domain.{User, StarredRepositoriesService}
import cats.effect.IO
import cats.effect._
import io.circe.{Decoder, Json}
import org.http4s.{Method, Request, Response}
import org.http4s.dsl.io._
import io.circe.syntax._
import org.http4s.circe._
import eu.timepit.refined.auto._
import org.specs2.mutable.Specification
import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import org.specs2.matcher.MatchResult
import githubsync.ErrorHandler._
import com.olegpy.meow.hierarchy._
import io.circe.generic.semiauto.deriveDecoder

class ServerTest extends Specification {
  "Service" >> {
    "returns 200" >> {
      val req = Request[IO](Method.GET, uri"/org/someorg/contributors")
      val r = Routes.contributorRoutes(testService).orNotFound(req).unsafeRunSync()
      r.status must beEqualTo(Status.Ok)
    }

    "returns valid json" >> {
      val req = Request[IO](Method.GET, uri"/org/owner/contributors")
      val r = Routes.contributorRoutes(testService).orNotFound(req).unsafeRunSync()
      r.status must beEqualTo(Status.Ok)
      r.as[List[User]].unsafeRunSync() must beEqualTo(desiredResult)
    }

    "returns 404" >> {
      val cont: StarredRepositoriesService[IO] = StarredRepositoriesService.create[IO](notFoundApi)
      val req = Request[IO](Method.GET, uri"/org/someorg/contributors")
      val r = Routes.contributorRoutes(cont).orNotFound(req).unsafeRunSync()
      r.status must beEqualTo(Status.BadRequest)
    }

    "return 500" >>{
      val cont: StarredRepositoriesService[IO] = StarredRepositoriesService.create[IO](errorApi)
      val req = Request[IO](Method.GET, uri"/org/someorg/contributors")
      val r = Routes.contributorRoutes(cont).orNotFound(req).unsafeRunSync()
      r.status must beEqualTo(Status.InternalServerError)
    }
  }

  implicit val contDecoder: Decoder[User] = deriveDecoder[User]
  implicit def contEntityDecoder: EntityDecoder[IO, List[User]] = jsonOf
  val testService: StarredRepositoriesService[IO] = StarredRepositoriesService.create[IO](testApi)

}
