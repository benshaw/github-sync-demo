package githubsync.domain

import cats.Applicative
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

object user{
  case class User(name: String, repo:String)

  case class UserError(e: Throwable) extends Throwable

  implicit val userEncoder: Encoder[User] = deriveEncoder[User]
  implicit def userEntityEncoder[F[_] : Applicative]: EntityEncoder[F, User] = jsonEncoderOf
}

