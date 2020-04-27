package githubsync

import cats.{Foldable, Monad}
import cats.effect.IO
import fs2.{INothing, Pure, Stream}
import githubsync.algebras.github.{GitHubAlgebra, _}
import githubsync.domain.repository._
import githubsync.domain.user.User
import githubsync.interpreters.upstream.domain.{GitHubOwner, GitHubRepository, GitHubStarGazer, GitHubStarredRepository, GitHubStarredRepositoryOwner}

import scala.collection.View.Empty
import scala.util.Random


object TestData {

  //! Raise errors instead of valid results
  val notFoundApi: GitHubApiAlgebra[IO] = new GitHubApiAlgebra[IO] {


    def stargazers(repo: Repository): Stream[IO, User] =
      Stream.raiseError[IO](ResourceNotFound(repo.name))

    def repositories(org: String): Stream[IO, Repository] =
      Stream.raiseError[IO](ResourceNotFound(org))


    def registerForRepoEvents(repo: Repository): IO[Repository] =
      IO.raiseError(ResourceNotFound(repo.name))

    def registerForStarEvents(repo: Repository): IO[Repository] =
      IO.raiseError(ResourceNotFound(repo.name))
  }

  val errorApi: GitHubApiAlgebra[IO] = new GitHubApiAlgebra[IO] {

    def stargazers(repo: Repository): Stream[IO, User] =
      Stream.raiseError[IO](ErrorRetrievingJson(repo.name))

    def repositories(org: String): Stream[IO, Repository] =
      Stream.raiseError[IO](ErrorRetrievingJson(org))


    def registerForRepoEvents(repo: Repository): IO[Repository] =
      IO.raiseError(UriParseError(repo.name))

    def registerForStarEvents(repo: Repository): IO[Repository] =
      IO.raiseError(UriParseError(repo.name))

  }

  private val r1 = "repo1"
  private val r2 = "repo2"
  private val r3 = "repo3"
  val s1 = "s1"
  val s2 = "s2"
  val s3 = "s3"
  val owner = "owner"
  val starGazersR1: Stream[Pure, User] = Stream(User(s1, r1), User(s2, r1), User(s3, r1))
  val starGazersR2: Stream[Pure, User] = Stream(User(s1, r2), User(s2, r2), User(s3, r2))

  val repo1 = Repository(r1, owner)
  val repo2 = Repository(r2, owner)
  val repo3 = Repository(r3, owner)

  val repos: Stream[Pure, Repository] =
    Stream(repo1, repo2, repo3)

  val gitHubRepo: Stream[Pure, GitHubRepository] =
    Stream(generateRepo(r1, owner),generateRepo(r2, owner),generateRepo(r3, owner))

  val gitHubStars: Stream[Pure, GitHubStarGazer] =
    Stream(genStar(s1),genStar(s2),genStar(s3))

    def genStar(name: String) =
      GitHubStarGazer(name, Random.nextDouble())

  def generateRepo(name: String, owner: String): GitHubRepository =
    GitHubRepository(id = Random.nextDouble(), node_id = Random.nextString(5), name = name, full_name = name,
      owner = GitHubOwner(login = owner, id = Random.nextDouble(), node_id = Some(name)),
      `private` = false)




}
