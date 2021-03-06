package githubsync.domain

import cats.Applicative
import cats.effect.Sync
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.circe.{jsonOf, _}
import org.http4s.{EntityDecoder, EntityEncoder}

// generated from https://json2caseclass.cleverapps.io/

object starevent {

  case class StarEventOwner(
                    login: String,
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
                    site_admin: Option[Boolean]
                  )

  case class StarEventRepository(
                                  id: Double,
                                  node_id: String,
                                  name: String,
                                  full_name: Option[String],
                                  `private`: Option[Boolean],
                                  owner: StarEventOwner,
                                  html_url: Option[String],
                                  description: Option[String],
                                  fork: Option[Boolean],
                                  url: Option[String],
                                  forks_url: Option[String],
                                  keys_url: Option[String],
                                  collaborators_url: Option[String],
                                  teams_url: Option[String],
                                  hooks_url: Option[String],
                                  issue_events_url: Option[String],
                                  events_url: Option[String],
                                  assignees_url: Option[String],
                                  branches_url: Option[String],
                                  tags_url: Option[String],
                                  blobs_url: Option[String],
                                  git_tags_url: Option[String],
                                  git_refs_url: Option[String],
                                  trees_url: Option[String],
                                  statuses_url: Option[String],
                                  languages_url: Option[String],
                                  stargazers_url: Option[String],
                                  contributors_url: Option[String],
                                  subscribers_url: Option[String],
                                  subscription_url: Option[String],
                                  commits_url: Option[String],
                                  git_commits_url: Option[String],
                                  comments_url: Option[String],
                                  issue_comment_url: Option[String],
                                  contents_url: Option[String],
                                  compare_url: Option[String],
                                  merges_url: Option[String],
                                  archive_url: Option[String],
                                  downloads_url: Option[String],
                                  issues_url: Option[String],
                                  pulls_url: Option[String],
                                  milestones_url: Option[String],
                                  notifications_url: Option[String],
                                  labels_url: Option[String],
                                  releases_url: Option[String],
                                  deployments_url: Option[String],
                                  created_at: Option[String],
                                  updated_at: Option[String],
                                  pushed_at: Option[String],
                                  git_url: Option[String],
                                  ssh_url: Option[String],
                                  clone_url: Option[String],
                                  svn_url: Option[String],
                                  homepage: Option[String],
                                  size: Option[Double],
                                  stargazers_count: Option[Double],
                                  watchers_count: Option[Double],
                                  language: Option[String],
                                  has_issues: Option[Boolean],
                                  has_projects: Option[Boolean],
                                  has_downloads: Option[Boolean],
                                  has_wiki: Option[Boolean],
                                  has_pages: Option[Boolean],
                                  forks_count: Option[Double],
                                  mirror_url: Option[String],
                                  archived: Option[Boolean],
                                  disabled: Option[Boolean],
                                  open_issues_count: Option[Double],
                                  license: Option[String],
                                  forks: Option[Double],
                                  open_issues: Option[Double],
                                  watchers: Option[Double],
                                  default_branch: Option[String]
                       )

  case class StarEventOrganization(
                           login: String,
                           id: Double,
                           node_id: Option[String],
                           url: Option[String],
                           repos_url: Option[String],
                           events_url: Option[String],
                           hooks_url: Option[String],
                           issues_url: Option[String],
                           members_url: Option[String],
                           public_members_url: Option[String],
                           avatar_url: Option[String],
                           description: Option[String]
                         )

  case class StarEvent(action: String,
                        starred_at: Option[String],
                        repository: StarEventRepository,
                        organization: StarEventOrganization,
                        sender: StarEventOwner)

  implicit val starEventRepoEncoder: Encoder[StarEventRepository] = deriveEncoder[StarEventRepository]
  implicit val starEventOrgEncoder: Encoder[StarEventOrganization] = deriveEncoder[StarEventOrganization]
  implicit val starEventOwnerEncoder: Encoder[StarEventOwner] = deriveEncoder[StarEventOwner]
  implicit val starEventEncoder: Encoder[StarEvent] = deriveEncoder[StarEvent]

  implicit def starEventEntityEncoder[F[_] : Applicative]: EntityEncoder[F, StarEvent] = jsonEncoderOf

  implicit val starEventRepoDecoder: Decoder[StarEventRepository] = deriveDecoder[StarEventRepository]
  implicit val starEventOrgDecoder: Decoder[StarEventOrganization] = deriveDecoder[StarEventOrganization]
  implicit val starEventOwnerDecoder: Decoder[StarEventOwner] = deriveDecoder[StarEventOwner]
  implicit val starEventDecoder: Decoder[StarEvent] = deriveDecoder[StarEvent]

  implicit def starEventEntityDecoder[F[_] : Sync]: EntityDecoder[F, StarEvent] = jsonOf

}
