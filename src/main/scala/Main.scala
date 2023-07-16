package mlb

import zio._
import zio.jdbc._
import zio.http._
import zio.stream.ZStream

object MlbApi extends ZIOAppDefault {

  val createZIOPoolConfig: ULayer[ZConnectionPoolConfig] =
    ZLayer.succeed(ZConnectionPoolConfig.default)

  val properties: Map[String, String] = Map(
    "user" -> "postgres",
    "password" -> "postgres"
  )

  val connectionPool: ZLayer[ZConnectionPoolConfig, Throwable, ZConnectionPool] =
    ZConnectionPool.h2mem(
      database = "testdb",
      props = properties
    )

  val endpoints: App[ZConnectionPool] =
    Http
      .collectZIO[Request] {
        case Method.GET -> Root => ZIO.from(Response.json("""{"response": "API works !"}"""))
        case Method.GET -> Root / "init" => {
          ZIO.from(Response.json("""{"response": "database initialised !"}"""))
        }
        case Method.GET -> Root / "season-playoff" => {
          for{
            result <- readAll
            response <- ZIO.from(chunkOfTwoToJson(result))
          } yield response
        }

        case Method.GET -> Root / "first-score" => {
          for {
            score <- readScore
            response <- ZIO.from(optionToJson(score))
          } yield response
        }

        case Method.GET -> Root / "predict" / "elo" / gameId => {
          for {
            _ <- Console.printLine(s"Prediction by elo for game: ${gameId}")
            data <- predictEloGame(gameId)
            response <- ZIO.from(optionToJson(data))
          } yield response
        }

        case Method.GET -> Root / "predict" / "rating" / gameId => {
          for {
            _ <- Console.printLine(s"Prediction by rating for game: ${gameId}")
            data <- predictRatingGame(gameId)
            response <- ZIO.from(optionToJson(data))
          } yield response
        }
      }
      .withDefaultErrorResponse

  val app: ZIO[ZConnectionPool & Server, Throwable, Unit] = for {
    conn <- create *> insertRows
    _ <- Console.printLine("Data Loaded")
    _ <- Server.serve(endpoints)
  } yield ()

  override def run: ZIO[Any, Throwable, Unit] =
    app.provide(createZIOPoolConfig >>> connectionPool, Server.default)
}