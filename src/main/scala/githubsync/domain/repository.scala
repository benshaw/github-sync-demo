package githubsync.domain

import cats.Applicative
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

object repository{

  case class Repository(name: String, owner: String)

  case class RepositoryError(e: Throwable) extends Throwable

  implicit private val repoEncoder: Encoder.AsObject[Repository] = deriveEncoder[Repository]
  implicit private def repoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Repository]] = jsonEncoderOf
}

