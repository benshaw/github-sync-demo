package githubsync.interpreters.upstream.domain

import cats.Applicative
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

// generated from https://json2caseclass.cleverapps.io/
case class GitHubWebHookRegistrationConfig(url: String,
                                           content_type: String,
                                           insecure_ssl: String)

case class GitHubWebHookRegistration(name: String,
                                     active: Boolean,
                                     events: List[String],
                                     config: GitHubWebHookRegistrationConfig)

object GitHubWebHookRegistration {
  implicit val ghwhrcEncoder: Encoder.AsObject[GitHubWebHookRegistrationConfig] = deriveEncoder[GitHubWebHookRegistrationConfig]
  implicit val ghwhrEncoder: Encoder.AsObject[GitHubWebHookRegistration] = deriveEncoder[GitHubWebHookRegistration]
  implicit def ghwhrEntityEncoder[F[_] : Applicative]: EntityEncoder[F,GitHubWebHookRegistration] = jsonEncoderOf
}
