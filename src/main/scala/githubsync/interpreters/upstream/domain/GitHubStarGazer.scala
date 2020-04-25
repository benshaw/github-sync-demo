package githubsync.interpreters.upstream.domain

import cats.effect.Sync
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

// generated from https://json2caseclass.cleverapps.io/
case class GitHubStarGazer(login: String,
                            id: Double,
                            node_id: Option[String],
                            avatar_url: Option[String],
                            gravatar_id: Option[String],
                            url: Option[String],
                            html_url: Option[String],
                            followers_url: Option[String],
                            following_url: Option[String],
                            gists_url: Option[String],
                            starred_url: Option[String],
                            subscriptions_url: Option[String],
                            organizations_url: Option[String],
                            repos_url: Option[String],
                            events_url: Option[String],
                            received_events_url: Option[String],
                            `type`: Option[String],
                            site_admin: Option[Boolean])

object GitHubStarGazer {

  implicit val githubstargazerDecoder: Decoder[GitHubStarGazer] = deriveDecoder[GitHubStarGazer]
  implicit def githubstargazerEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[GitHubStarGazer]] = jsonOf
}


