package githubsync.interpreters.githubapi.models

import cats.effect.Sync
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

// generated from https://json2caseclass.cleverapps.io/

case class GitHubContributor(login: String,
                             id: Int,
                             node_id: String,
                             `type`: String,
                             contributions: Int,
                             site_admin: Option[Boolean]= None,
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
                             received_events_url: Option[String] = None)

object GitHubContributor {
  implicit val contributorListDecoder: Decoder[GitHubContributor] = deriveDecoder[GitHubContributor]
  implicit def contributorEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[GitHubContributor]] = jsonOf
}
