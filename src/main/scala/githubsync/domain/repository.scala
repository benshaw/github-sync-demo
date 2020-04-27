package githubsync.domain

import cats.Applicative
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

object repository{

  case class Repository(name: String, owner: String)

  case class RepositoryError(e: Throwable) extends Throwable

  implicit val repoEncoder: Encoder.AsObject[Repository] = deriveEncoder[Repository]
  implicit def repoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Repository] = jsonEncoderOf
}

