package githubsync

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

object Main extends IOApp {

  def run(args: List[String]) = {

    implicit val logger = Slf4jLogger.getLogger[IO]

    for {
      config <- Server.config.load[IO]
      server <- Server.stream[IO](config).compile.drain.as(ExitCode.Success)
    } yield server
  }
}