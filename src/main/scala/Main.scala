package mlb

import zio._
import zio.jdbc._
import zio.http._

object MlbApi extends ZIOAppDefault {

  val createZIOPoolConfig: ULayer[ZConnectionPoolConfig] =
    ZLayer.succeed(ZConnectionPoolConfig.default)

  val properties: Map[String, String] = Map(
    "user" -> "postgres",
    "password" -> "postgres"
  )

  val connectionPool : ZLayer[ZConnectionPoolConfig, Throwable, ZConnectionPool] =
    ZConnectionPool.h2mem(
      database = "testdb",
      props = properties
    )

  val create: ZIO[ZConnectionPool, Throwable, Unit] = transaction {
    execute(
      sql"CREATE TABLE IF NOT EXISTS mytable(date DATE NOT NULL PRIMARY KEY, season INTEGER NOT NULL, neutral BIT NOT NULL, playoff VARCHAR(30), team1 VARCHAR(3) NOT NULL, team2 VARCHAR(3) NOT NULL, elo1_pre VARCHAR(16) NOT NULL, elo2_pre VARCHAR(16) NOT NULL, elo_prob1 VARCHAR(19) NOT NULL, elo_prob2 VARCHAR(19) NOT NULL, elo1_post VARCHAR(16), elo2_post VARCHAR(16), rating1_pre VARCHAR(16) NOT NULL, rating2_pre VARCHAR(16) NOT NULL, pitcher1 VARCHAR(20), pitcher2 VARCHAR(20), pitcher1_rgs VARCHAR(16), pitcher2_rgs VARCHAR(16), pitcher1_adj VARCHAR(21), pitcher2_adj VARCHAR(21), rating_prob1 VARCHAR(19) NOT NULL, rating_prob2 VARCHAR(19) NOT NULL, rating1_post VARCHAR(16), rating2_post VARCHAR(16), score1 INTEGER, score2 INTEGER);"
    )
  }

  val endpoints: App[Any] =
    Http
      .collect[Request] {
        case Method.GET -> Root => Response.json("""{"response": "API works !"}""")
        case Method.GET -> Root / "init" => {
          Response.json("""{"response": "database initialised !"}""")
        }
        case Method.GET -> Root / "games" => ???
        case Method.GET -> Root / "predict" / "game" / gameId => ???
      }
      .withDefaultErrorResponse

  val app: ZIO[ZConnectionPool & Server, Throwable, Unit] = for {
    conn <- create *> ZIO.from({
      val values: Data = csvToList("./csv/mlb_elo_latest.csv")
      for (line <- values) {
        insertRows(line)
          .catchAll({ error => 
            ZIO.die(error)
          })
      }
    })
    _ <- Server.serve(endpoints)
  } yield ()

  override def run: ZIO[Any, Throwable, Unit] =
    app.provide(createZIOPoolConfig >>> connectionPool, Server.default)
}