package githubsync.domain

import cats.effect.IO

class UserServiceTest extends org.specs2.mutable.Specification{

  import githubsync.TestData._

  val cont = StarredRepositoriesService.create[IO](testApi)
  "Contributors" >> {
    "Are calculated correctly" >> {
      cont
        .get
        .run(owner)
        .unsafeRunSync() must containTheSameElementsAs(desiredResult)
    }
  }
}

