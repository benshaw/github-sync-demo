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

case class GitHubStarredRepositoryOwner(login: String,
                                         id: Option[Double] = None,
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

case class GitHubStarredRepositoryPermissions(admin: Option[Boolean] = None,
                                               push: Option[Boolean] = None,
                                               pull: Option[Boolean] = None)

case class GitHubStarredRepository(name: String,
                                    owner: GitHubStarredRepositoryOwner,
                                    id: Option[Double] = None,
                                    node_id: Option[String] = None,
                                    full_name: Option[String] = None,
                                    `private`: Option[Boolean] = None,
                                    html_url: Option[String] = None,
                                    description: Option[String] = None,
                                    fork: Option[Boolean] = None,
                                    url: Option[String] = None,
                                    archive_url: Option[String] = None,
                                    assignees_url: Option[String] = None,
                                    blobs_url: Option[String] = None,
                                    branches_url: Option[String] = None,
                                    collaborators_url: Option[String] = None,
                                    comments_url: Option[String] = None,
                                    commits_url: Option[String] = None,
                                    compare_url: Option[String] = None,
                                    contents_url: Option[String] = None,
                                    contributors_url: Option[String] = None,
                                    deployments_url: Option[String] = None,
                                    downloads_url: Option[String] = None,
                                    events_url: Option[String] = None,
                                    forks_url: Option[String] = None,
                                    git_commits_url: Option[String] = None,
                                    git_refs_url: Option[String] = None,
                                    git_tags_url: Option[String] = None,
                                    git_url: Option[String] = None,
                                    issue_comment_url: Option[String] = None,
                                    issue_events_url: Option[String] = None,
                                    issues_url: Option[String] = None,
                                    keys_url: Option[String] = None,
                                    labels_url: Option[String] = None,
                                    languages_url: Option[String] = None,
                                    merges_url: Option[String] = None,
                                    milestones_url: Option[String] = None,
                                    notifications_url: Option[String] = None,
                                    pulls_url: Option[String] = None,
                                    releases_url: Option[String] = None,
                                    ssh_url: Option[String] = None,
                                    stargazers_url: Option[String] = None,
                                    statuses_url: Option[String] = None,
                                    subscribers_url: Option[String] = None,
                                    subscription_url: Option[String] = None,
                                    tags_url: Option[String] = None,
                                    teams_url: Option[String] = None,
                                    trees_url: Option[String] = None,
                                    clone_url: Option[String] = None,
                                    mirror_url: Option[String] = None,
                                    hooks_url: Option[String] = None,
                                    svn_url: Option[String] = None,
                                    homepage: Option[String] = None,
                                    language: Option[String] = None,
                                    forks_count: Option[Double] = None,
                                    stargazers_count: Option[Double] = None,
                                    watchers_count: Option[Double] = None,
                                    size: Option[Double] = None,
                                    default_branch: Option[String] = None,
                                    open_issues_count: Option[Double] = None,
                                    is_template: Option[Boolean] = None,
                                    topics: Option[List[String]] = None,
                                    has_issues: Option[Boolean] = None,
                                    has_projects: Option[Boolean] = None,
                                    has_wiki: Option[Boolean] = None,
                                    has_pages: Option[Boolean] = None,
                                    has_downloads: Option[Boolean] = None,
                                    archived: Option[Boolean] = None,
                                    disabled: Option[Boolean] = None,
                                    visibility: Option[String] = None,
                                    pushed_at: Option[String] = None,
                                    created_at: Option[String] = None,
                                    updated_at: Option[String] = None,
                                    permissions: Option[GitHubStarredRepositoryPermissions] = None,
                                    allow_rebase_merge: Option[Boolean] = None,
                                    template_repository: Option[String] = None,
                                    temp_clone_token: Option[String] = None,
                                    allow_squash_merge: Option[Boolean] = None,
                                    allow_merge_commit: Option[Boolean] = None,
                                    subscribers_count: Option[Double] = None,
                                    network_count: Option[Double] = None)

object GitHubStarredRepository {
  implicit val starredOwnerDecoder = deriveDecoder[GitHubStarredRepositoryOwner]
  implicit val starredPermissionsDecoder = deriveDecoder[GitHubStarredRepositoryPermissions]
  implicit val starredRepoListDecoder = deriveDecoder[GitHubStarredRepository]

  implicit def starredRepoEntityDecoder[F[_] : Sync]: EntityDecoder[F, GitHubStarredRepository] = jsonOf
}

