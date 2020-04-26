package githubsync.interpreters.upstream.domain

import cats.effect.Sync
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.http4s.EntityDecoder
import cats.effect.Sync
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

// generated from https://json2caseclass.cleverapps.io/

case class GitHubStarredRepositoryOwner(
                  login: Option[String],
                  id: Option[Double],
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
                  repos_url: String,
                  events_url: Option[String],
                  received_events_url: Option[String],
                  `type`: Option[String],
                  site_admin: Option[Boolean]
                )

case class GitHubStarredRepositoryPermissions(
                        admin: Option[Boolean],
                        push: Option[Boolean],
                        pull: Option[Boolean]
                      )

case class GitHubStarredRepository(
                           id: Option[Double],
                           node_id: Option[String],
                           name: Option[String],
                           full_name: Option[String],
                           owner: GitHubStarredRepositoryOwner,
                           `private`: Option[Boolean],
                           html_url: String,
                           description: Option[String],
                           fork: Option[Boolean],
                           url: Option[String],
                           archive_url: Option[String],
                           assignees_url: Option[String],
                           blobs_url: Option[String],
                           branches_url: Option[String],
                           collaborators_url: Option[String],
                           comments_url: Option[String],
                           commits_url: Option[String],
                           compare_url: Option[String],
                           contents_url: Option[String],
                           contributors_url: Option[String],
                           deployments_url: Option[String],
                           downloads_url: Option[String],
                           events_url: Option[String],
                           forks_url: Option[String],
                           git_commits_url: Option[String],
                           git_refs_url: Option[String],
                           git_tags_url: Option[String],
                           git_url: Option[String],
                           issue_comment_url: Option[String],
                           issue_events_url: Option[String],
                           issues_url: Option[String],
                           keys_url: Option[String],
                           labels_url: Option[String],
                           languages_url: Option[String],
                           merges_url: Option[String],
                           milestones_url: Option[String],
                           notifications_url: Option[String],
                           pulls_url: Option[String],
                           releases_url: Option[String],
                           ssh_url: Option[String],
                           stargazers_url: Option[String],
                           statuses_url: Option[String],
                           subscribers_url: Option[String],
                           subscription_url: Option[String],
                           tags_url: Option[String],
                           teams_url: Option[String],
                           trees_url: Option[String],
                           clone_url: Option[String],
                           mirror_url: Option[String],
                           hooks_url: Option[String],
                           svn_url: Option[String],
                           homepage: Option[String],
                           language: Option[String],
                           forks_count: Option[Double],
                           stargazers_count: Option[Double],
                           watchers_count: Option[Double],
                           size: Option[Double],
                           default_branch: Option[String],
                           open_issues_count: Option[Double],
                           is_template: Option[Boolean],
                           topics: Option[List[String]],
                           has_issues: Option[Boolean],
                           has_projects: Option[Boolean],
                           has_wiki: Option[Boolean],
                           has_pages: Option[Boolean],
                           has_downloads: Option[Boolean],
                           archived: Option[Boolean],
                           disabled: Option[Boolean],
                           visibility: Option[String],
                           pushed_at: Option[String],
                           created_at: Option[String],
                           updated_at: Option[String],
                           permissions: Option[GitHubStarredRepositoryPermissions],
                           allow_rebase_merge: Option[Boolean],
                           template_repository: Option[String],
                           temp_clone_token: Option[String],
                           allow_squash_merge: Option[Boolean],
                           allow_merge_commit: Option[Boolean],
                           subscribers_count: Option[Double],
                           network_count: Option[Double]
                         )

object GitHubStarredRepository {
  implicit val starredOwnerDecoder = deriveDecoder[GitHubStarredRepositoryOwner]
  implicit val starredPermissionsDecoder = deriveDecoder[GitHubStarredRepositoryPermissions]
  implicit val starredRepoListDecoder = deriveDecoder[GitHubStarredRepository]
  implicit def starredRepoEntityDecoder[F[_] : Sync]: EntityDecoder[F,GitHubStarredRepository] = jsonOf
}

