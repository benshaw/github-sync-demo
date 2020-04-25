package githubsync.interpreters.upstream

import cats.effect.Sync
import cats.implicits._
import fs2.{Pipe, Pure, RaiseThrowable, Stream, text}
import githubsync.algebras.github.JsonDecodingError
import io.circe.fs2._
import io.circe.{Decoder, Json}
import org.http4s.client.Client
import org.http4s.headers.{Link, LinkValue}
import org.http4s.{EntityBody, EntityDecoder, Headers, Request}
import io.circe.Json
import jawnfs2._

object PaginatedClientWithGithubErrors {

  def jsonDecoder[F[_]: RaiseThrowable, A](implicit decode: Decoder[A]): Pipe[F, Json, A] =
    _.flatMap { json =>
      decode(json.hcursor) match {
        case Left(df) => {
          val err: String = df.show
          Stream.raiseError[F](JsonDecodingError(err))
        }
        case Right(a) => Stream.emit(a)
      }
    }

  // jawn-fs2 needs to know what JSON AST you want
  implicit val f = new io.circe.jawn.CirceSupportParser(None, false).facade

  implicit class PaginatedClient[F[_]](c: Client[F])(implicit F: Sync[F]) {

    case class Rep(headers: Headers, body: EntityBody[F])

    //! Uses the link header and page query param to get all pages
    //! Any errors use the GitHubError ADT
    //! https://tools.ietf.org/html/rfc5988
    //! https://developer.github.com/v3/#pagination
    //! This works of the assumption that nextPage is always currpage+1, i have not been able to find anything that suggests this is incorrect
    //! If so an alternative would be a recurcive solution but it would incurr additional complexity
    def getAllPages[T](req: Request[F])(implicit js: Pipe[F, Json, T])/*(implicit ft: EntityDecoder[F, List[T]])*/: Stream[F, T] = {

      def range(x: Rep): Stream[Pure, Int] = (for {
        next <- nextPage(x.headers)
        last <- lastPage(x.headers)
      } yield Stream.range(next, last + 1)).getOrElse(Stream.empty)//range is not inclusive

      def requestPage(page: Int)/*(implicit ft: EntityDecoder[F, List[T]])*/: Stream[F, Rep] =
        c.stream(addPageToRequest(page, req))
          .map(r => Rep(r.headers, r.body))

      //! Get the first page of data and headers
      val pg1: Stream[F, Rep] = requestPage(1)
      //! Build a list of remaining pages using the data received in the header
      val remainingPages: Stream[F, Rep] = pg1.flatMap(range(_).flatMap(requestPage)) //! \todo can these be run concurrently ?
      val result: Stream[F, Json] = (pg1 ++ remainingPages).flatMap(_.body.chunks.unwrapJsonArray)

      result.through(js)
    }

    private def extractPage(l: LinkValue): Option[Int] = {
      val regex = "(?<=page=)([0-9*])".r
      regex.findFirstIn(l.toString()).flatMap(_.toIntOption)
    }

    private def nextPage(h: Headers): Option[Int] =
      getPage(h, rel = "next")

    private def getPage(h: Headers, rel: String) =
      h.get(Link).flatMap(_.values.find(_.rel.contains(rel))).flatMap(extractPage)

    private def lastPage(h: Headers): Option[Int] =
      getPage(h, rel = "last")

    private def addPageToRequest(page: Int, req: Request[F]): Request[F] =
      Request(method = req.method,
        uri = req.uri.withQueryParam("page", page),
        httpVersion = req.httpVersion,
        headers = req.headers,
        body = req.body,
        attributes = req.attributes)


  }

}
