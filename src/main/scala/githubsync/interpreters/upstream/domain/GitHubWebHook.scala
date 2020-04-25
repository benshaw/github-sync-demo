package githubsync.interpreters.upstream.domain

case class Config(url: String,
                   content_type: String,
                   insecure_ssl: String)

case class GitHubWebHook(name: String,
                         active: Boolean,
                         events: List[String],
                         config: Config)
