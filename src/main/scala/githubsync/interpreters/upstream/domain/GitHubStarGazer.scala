package githubsync.interpreters.upstream.domain

import cats.effect.Sync
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

// generated from https://json2caseclass.cleverapps.io/
case class GitHubStarGazer(login: String,
                            id: Double,
                            node_id: Option[String] = None,
                            avatar_url: Option[String] = None,
                            gravatar_id: Option[String] = None,
                            url: Option[String] = None,
                            html_url: Option[String] = None,
                            followers_url: Option[String] = None,
                            following_url: Option[String] = None,
                            gists_url: Option[String] = None,
                            starred_url: Option[String] = None,
                            subscriptions_url: Option[String] = None,
                            organizations_url: Option[String] = None,
                            repos_url: Option[String] = None,
                            events_url: Option[String] = None,
                            received_events_url: Option[String] = None,
                            `type`: Option[String] = None,
                            site_admin: Option[Boolean] = None)

object GitHubStarGazer {

  implicit val githubstargazerDecoder: Decoder[GitHubStarGazer] = deriveDecoder[GitHubStarGazer]
  implicit def githubstargazerEntityDecoder[F[_] : Sync]: EntityDecoder[F, GitHubStarGazer] = jsonOf
}


