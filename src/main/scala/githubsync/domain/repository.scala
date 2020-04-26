package githubsync.domain

object repository{

  case class Repository(name: String, owner: String)

  case class RepositoryError(e: Throwable) extends Throwable
}

