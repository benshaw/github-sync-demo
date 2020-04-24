package githubsync.interpreters.persistent

import cats.effect.Sync
import githubsync.domain.GitHubApi.{GitHubApiAlgebra, GitHubPersistentStoreAlgebra}
import githubsync.interpreters.upstream.GitHubApiInterpreter.GitHubApiConfig
import org.http4s.client.Client


object GitHubPersistentStoreInterpreter {
 /* def create[F[_] : Sync](client: Client[F], config: GitHubApiConfig) =
    new GitHubPersistentStoreAlgebra[F] {

    }*/

}
