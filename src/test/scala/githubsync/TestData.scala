package githubsync

import githubsync.domain.{Repository, User}
import githubsync.algebras.GitHubApi._
import githubsync.interpreters.{GitHubLicense, GitHubOwner, GitHubPermissions, GitHubRepository}
import cats.Applicative
import cats.effect.{IO, Sync}
import githubsync.interpreters.upstream.domain.{GitHubContributor, GitHubLicense, GitHubOwner, GitHubPermissions, GitHubRepository}
import githubsync.interpreters.upstream.{GitHubLicense, GitHubOwner, GitHubPermissions, GitHubRepository}
import io.circe.{Decoder, Encoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.jsonEncoderOf
import io.circe.generic.semiauto._
import org.http4s.circe._

import scala.util.Random


object TestData {
  //! This test API returns constant data
  //! It has 3 repos each with 3 contributors
  //! Contributor 1 has 1 contribution in each repo contributor 2  has 2 and so on
  //! This should end up with contributor 1 having 3 total contributions contributor 2 having 6 and contributor 3 having 9
  val testApi: GitHubApiAlgebra[IO] = new GitHubApiAlgebra[IO] {
    def contributors(repo: Repository): IO[List[User]] = {
      IO.pure(contrib((repo.name, repo.owner)))
    }

    def repositories(org: String): IO[List[Repository]] = {
      IO.pure(repos)
    }

  }
  //! Raise errors instead of valid results
  val notFoundApi: GitHubApiAlgebra[IO] = new GitHubApiAlgebra[IO] {
    def contributors(repo: Repository): IO[List[User]] = {
      IO.raiseError(ResourceNotFound(repo.name))
    }

    def repositories(org: String): IO[List[Repository]] = {
      IO.raiseError(ResourceNotFound(org))
    }
  }

  val errorApi: GitHubApiAlgebra[IO] = new GitHubApiAlgebra[IO] {
    def contributors(repo: Repository): IO[List[User]] = {
      IO.raiseError(ErrorRetrievingJson(repo.name))
    }

    def repositories(org: String): IO[List[Repository]] = {
      IO.raiseError(ErrorRetrievingJson(org))
    }
  }

  private val repo1 = "repo1"
  private val repo2 = "repo2"
  private val repo3 = "repo3"
  val cont1 = "cont1"
  val cont2 = "cont2"
  val cont3 = "cont3"
  val owner = "owner"
  val cont1Result = 3
  val cont2Result = 6
  val cont3Result = 9
  val cont = (User(cont1, 1) :: User(cont2, 2) :: User(cont3, 3) :: Nil)

  val desiredResult: List[User] =
    (User(cont1, cont1Result) :: User(cont2, cont2Result) :: User(cont3, cont3Result) :: Nil).sortBy(_.contributions)(Ordering.Int.reverse)

  val repos: List[Repository] =
    Repository(repo1, owner) :: Repository(repo2, owner) :: Repository(repo3, owner) :: Nil

  val contrib: Map[(String, String), List[User]] = Map(
    (repo1, owner) -> cont,
    (repo2, owner) -> cont,
    (repo3, owner) -> cont
  )

  val gitHubCont = (generateContr(cont1, 1) :: generateContr(cont2, 2) :: generateContr(cont3, 3) :: Nil)

  val gitHubRepo =
    generateRepo(repo1, owner) :: generateRepo(repo2, owner) :: generateRepo(repo3, owner) :: Nil

  def generateContr(name: String, num: Int) =
    GitHubContributor(
      login = name,
      id = Random.nextInt(),
      node_id = name,
      `type` = "User",
      contributions = num)

  def generateRepo(name: String, owner: String): Repository =
    GitHubRepository(id = Random.nextDouble(), node_id = Random.nextString(5), name = name, full_name = name,
      owner = GitHubOwner(login = owner, id = Random.nextDouble(), node_id = name),
      `private` = false)


  implicit val repoEncoder: Encoder[Repository] = deriveEncoder[Repository]
  implicit def repoEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Repository]] = jsonEncoderOf
  implicit val repoDecoder: Decoder[Repository] = deriveDecoder[Repository]
  implicit def repoEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[Repository]] = jsonOf
  implicit val contEncoder: Encoder[User] = deriveEncoder[User]
  implicit def contEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[User]] = jsonEncoderOf
  implicit val contDecoder: Decoder[User] = deriveDecoder[User]
  implicit def contEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[User]] = jsonOf
  implicit val ghEncoder: Encoder[Repository] = deriveEncoder[Repository]
  implicit val ghoEncoder: Encoder[GitHubOwner] = deriveEncoder[GitHubOwner]
  implicit val ghlEncoder: Encoder.AsObject[GitHubLicense] = deriveEncoder[GitHubLicense]
  implicit val ghpEncoder: Encoder.AsObject[GitHubPermissions] = deriveEncoder[GitHubPermissions]
  implicit def ghEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Repository]] = jsonEncoderOf
  implicit val conEncoder: Encoder.AsObject[GitHubContributor] = deriveEncoder[GitHubContributor]
  implicit def conEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[GitHubContributor]] = jsonEncoderOf


}
