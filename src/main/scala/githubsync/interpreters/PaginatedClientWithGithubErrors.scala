package githubsync.interpreters

import githubsync.domain.GitHubApi.{ErrorRetrievingJson, JsonDecodingError, ResourceNotFound}
import cats.effect.Sync
import cats.implicits._
import org.http4s.client.Client
import org.http4s.headers.{Link, LinkValue}
import org.http4s.{EntityDecoder, Headers, Request, Status}

object PaginatedClientWithGithubErrors {

  import githubsync.interpreters.githubapi.GitHubUtilityErrors._

  implicit class PaginatedClient[F[_]](c: Client[F])(implicit F: Sync[F]) {

    //! Uses the link header and page query param to get all pages
    //! Any errors use the GitHubError ADT
    //! https://tools.ietf.org/html/rfc5988
    //! https://developer.github.com/v3/#pagination
    //! This works of the assumption that nextPage is always currpage+1, i have not been able to find anything that suggests this is incorrect
    //! If so an alternative would be a recurcive solution but it would incurr additional complexity
    def getAllPages[T](req: Request[F])(implicit ft: EntityDecoder[F, List[T]]): F[List[T]] = {

      def requestPage(page: Int)(implicit ft: EntityDecoder[F, List[T]]): F[(Headers, List[T])] =
        c.fetch(addPageToRequest(page, req)) {
          case Status.Successful(r) =>
            r
              .attempt[List[T]](e => JsonDecodingError(e.message))
              .map((r.headers, _))

          case r =>
            r.as[String]
              .flatMap(s => r.status match {
                case Status.NotFound => F.raiseError(ResourceNotFound(s))//F.pure(r.headers, List.empty)//Ignore 404 errors as repos that the user does not have permissions for will return this
                case _ => F.raiseError(ErrorRetrievingJson(s))
              })
        }

      //! Get the first page of data and headers
      requestPage(1).flatMap { x =>

        //! Build a list of remaining pages using the data received in the header
        val r: List[Int] = for {
          next <- nextPage(x._1).toList
          last <- lastPage(x._1).toList
          r <- List.range(next, last + 1) //range is not inclusive
        } yield r

        val remainingPages: F[List[T]] = r.flatTraverse(requestPage(_).map(_._2)) //! get the results for the rest of the pages if any

        remainingPages.map(_ ::: x._2) //! combine the result sets
      }
    }

    private def extractPage(l: LinkValue): Option[Int] = {
      val regex = "(?<=page=)([0-9*])".r
      regex.findFirstIn(l.toString()).flatMap(_.toIntOption)
    }

    private def nextPage(h: Headers): Option[Int] =
      h.get(Link).flatMap(_.values.find(_.rel.contains("next"))).flatMap(extractPage)

    private def lastPage(h: Headers): Option[Int] =
      h.get(Link).flatMap(_.values.find(_.rel.contains("last"))).flatMap(extractPage)

    private def addPageToRequest(page: Int, req: Request[F]): Request[F] =
        Request(method = req.method,
          uri = req.uri.withQueryParam("page",page),
          httpVersion = req.httpVersion,
          headers = req.headers,
          body = req.body,
          attributes = req.attributes)

  }

}
