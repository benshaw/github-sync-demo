package githubsync

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object Main extends IOApp {

  def run(args: List[String]) =
    for {
      config <- Server.config.load[IO]
      server <- Server.stream[IO](config).compile.drain.as(ExitCode.Success)
    } yield server
}