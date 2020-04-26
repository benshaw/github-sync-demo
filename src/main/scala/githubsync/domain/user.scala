package githubsync.domain

object user{
  case class User(name: String, repo:String)

  case class UserError(e: Throwable) extends Throwable
}

