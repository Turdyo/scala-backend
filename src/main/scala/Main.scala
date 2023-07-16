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

  val create: ZIO[ZConnectionPool, Throwable, Unit] = transaction {
    execute(
      sql"""
      CREATE TABLE IF NOT EXISTS baseballElo(
        match_date VARCHAR(10) NOT NULL,
        season INTEGER NOT NULL,
        neutral INTEGER NOT NULL,
        playoff VARCHAR(30),
        team1 VARCHAR(3) NOT NULL,
        team2 VARCHAR(3) NOT NULL,
        elo1_pre VARCHAR(16) NOT NULL,
        elo2_pre VARCHAR(16) NOT NULL,
        elo_prob1 VARCHAR(19) NOT NULL,
        elo_prob2 VARCHAR(19) NOT NULL,
        elo1_post VARCHAR(16),
        elo2_post VARCHAR(16),
        rating1_pre VARCHAR(16) NOT NULL,
        rating2_pre VARCHAR(16) NOT NULL,
        pitcher1 VARCHAR(20),
        pitcher2 VARCHAR(20),
        pitcher1_rgs VARCHAR(16),
        pitcher2_rgs VARCHAR(16),
        pitcher1_adj VARCHAR(21),
        pitcher2_adj VARCHAR(21),
        rating_prob1 VARCHAR(19) NOT NULL,
        rating_prob2 VARCHAR(19) NOT NULL,
        rating1_post VARCHAR(16),
        rating2_post VARCHAR(16),
        score1 INTEGER,
        score2 INTEGER);"""
    )
  }

  val insertRows: ZIO[ZConnectionPool, Throwable, List[UpdateResult]] = transaction {
    val csvList = csvToList("./csv/mlb_elo_latest.csv")
    val queries = csvList.map(line => {
      insert(
        sql"INSERT INTO baseballElo VALUES(${line(0)}, ${line(1)}, ${line(2)}, ${line(3)}, ${line(4)}, ${line(5)}, ${line(6)}, ${line(7)}, ${line(8)}, ${line(9)}, ${line(10)}, ${line(11)}, ${line(12)}, ${line(13)}, ${line(14)}, ${line(15)}, ${line(16)}, ${line(17)}, ${line(18)}, ${line(19)}, ${line(20)}, ${line(21)}, ${line(22)}, ${line(23)}, ${line(24)}, ${line(25)})"
      )
    })
    ZIO.collectAll(queries)
  }

  val readAll: ZIO[ZConnectionPool, Throwable, Chunk[(Int, String)]] = transaction {
    selectAll(sql"SELECT season, playoff FROM baseballElo".as[(Int, String)])
  }

  val readScore: ZIO[ZConnectionPool, Throwable, Option[Int]] = transaction {
    selectOne(sql"SELECT score1 from baseballElo".as[Int])
  }

  val endpoints: App[ZConnectionPool] =
    Http
      .collectZIO[Request] {
        case Method.GET -> Root => ZIO.from(Response.json("""{"response": "API works !"}"""))
        case Method.GET -> Root / "init" => {
          ZIO.from(Response.json("""{"response": "database initialised !"}"""))
        }
        case Method.GET -> Root / "games" => ???

        case Method.GET -> Root / "test" => {
          for {
            // data <- readAll
            // collectedData <- ZIO.from(data.collect {case (season, playoff) => })
            score <- readScore
            response <- ZIO.from(Response.json(s"""{"response": "${score}"}"""))
          } yield response
        }

        case Method.GET -> Root / "predict" / "game" / gameId => ???
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