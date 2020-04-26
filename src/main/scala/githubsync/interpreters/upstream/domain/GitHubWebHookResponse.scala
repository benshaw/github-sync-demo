package githubsync.interpreters.upstream.domain

import cats.effect.Sync
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

// generated from https://json2caseclass.cleverapps.io/
case class GitHubWebHookResponseConfig(content_type: Option[String],
                                        insecure_ssl: Option[String],
                                        url: Option[String])

case class GitHubWebHookLastResponse(code: Option[String],
                                      status: Option[String],
                                      message: Option[String])

case class GitHubWebHookResponse(`type`: String,
                                  id: Double,
                                  name: String,
                                  active: Boolean,
                                  events: Option[List[String]],
                                  config: Option[GitHubWebHookResponseConfig],
                                  updated_at: Option[String],
                                  created_at: Option[String],
                                  url: Option[String],
                                  test_url: Option[String],
                                  ping_url: Option[String],
                                  last_response: Option[GitHubWebHookLastResponse])

object GitHubWebHookResponse {
  implicit val ghwhcDecoder: Decoder[GitHubWebHookResponseConfig] = deriveDecoder[GitHubWebHookResponseConfig]
  implicit val ghwhlrDecoder: Decoder[GitHubWebHookLastResponse] = deriveDecoder[GitHubWebHookLastResponse]
  implicit val ghwhDecoder: Decoder[GitHubWebHookResponse] = deriveDecoder[GitHubWebHookResponse]
  implicit def ghwhEntityDecoder[F[_] : Sync]: EntityDecoder[F, GitHubWebHookResponse] = jsonOf
}

