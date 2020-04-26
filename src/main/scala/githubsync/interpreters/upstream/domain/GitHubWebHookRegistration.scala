package githubsync.interpreters.upstream.domain

// generated from https://json2caseclass.cleverapps.io/
case class GitHubWebHookRegistrationConfig(url: String,
                                           content_type: String,
                                           insecure_ssl: String)

case class GitHubWebHookRegistration(name: String,
                                     active: Boolean,
                                     events: List[String],
                                     config: GitHubWebHookRegistrationConfig)
