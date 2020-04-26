package githubsync
import org.specs2.mutable.Specification

class ServerTest extends Specification {

  /*
  "Service" >> {
    "returns 200" >> {
      val req = Request[IO](Method.GET, uri"/org/someorg/contributors")
      val r = Routes.repositoryRoutes(testService).orNotFound(req).unsafeRunSync()
      r.status must beEqualTo(Status.Ok)
    }

    "returns valid json" >> {
      val req = Request[IO](Method.GET, uri"/org/owner/contributors")
      val r = Routes.repositoryRoutes(testService).orNotFound(req).unsafeRunSync()
      r.status must beEqualTo(Status.Ok)
      r.as[List[User]].unsafeRunSync() must beEqualTo(desiredResult)
    }

    "returns 404" >> {
      val cont: RepositoryService[IO] = repositoryserviceinterpreter.RepositoryServiceInterpreter[IO](notFoundApi)
      val req = Request[IO](Method.GET, uri"/org/someorg/contributors")
      val r = Routes.repositoryRoutes(cont).orNotFound(req).unsafeRunSync()
      r.status must beEqualTo(Status.BadRequest)
    }

    "return 500" >>{
      val cont: RepositoryService[IO] = repositoryserviceinterpreter.RepositoryServiceInterpreter[IO](errorApi)
      val req = Request[IO](Method.GET, uri"/org/someorg/contributors")
      val r = Routes.repositoryRoutes(cont).orNotFound(req).unsafeRunSync()
      r.status must beEqualTo(Status.InternalServerError)
    }
  }

  implicit val contDecoder: Decoder[User] = deriveDecoder[User]
  implicit def contEntityDecoder: EntityDecoder[IO, List[User]] = jsonOf
  val testService: RepositoryService[IO] = repositoryserviceinterpreter.RepositoryServiceInterpreter[IO](testApi)

  */
}
