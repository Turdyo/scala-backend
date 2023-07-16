package mlb

import zio._
import zio.jdbc._
import zio.http._

val create: ZIO[ZConnectionPool, Throwable, Unit] = transaction {
  execute(
    sql"""
      CREATE TABLE IF NOT EXISTS match(
      id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
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
  val queries = csvList.zipWithIndex.map((line, index) => {
    insert(
      sql"INSERT INTO match VALUES(${index}, ${line(0)}, ${line(1)}, ${line(2)}, ${line(3)}, ${line(4)}, ${line(
          5
        )}, ${line(6)}, ${line(7)}, ${line(8)}, ${line(9)}, ${line(10)}, ${line(11)}, ${line(12)}, ${line(13)}, ${line(
          14
        )}, ${line(15)}, ${line(16)}, ${line(17)}, ${line(18)}, ${line(19)}, ${line(20)}, ${line(21)}, ${line(
          22
        )}, ${line(23)}, ${line(24)}, ${line(25)})"
    )
  })
  ZIO.collectAll(queries)
}

val readAll: ZIO[ZConnectionPool, Throwable, Chunk[Match]] = transaction {
  selectAll(
    sql"SELECT id, match_date, season, team1, team2, score1, score2 FROM match ORDER BY match_date DESC".as[Match]
  )
}

def readMatch(match_id: String): ZIO[ZConnectionPool, Throwable, Option[Match]] = transaction {
  selectOne(sql"SELECT id, match_date, season, team1, team2, score1, score2 FROM match WHERE id = ${match_id}".as[Match])
}

def readBySeason(season: String): ZIO[ZConnectionPool, Throwable, Chunk[Match]] = transaction {
  selectAll(
    sql"SELECT id, match_date, season, team1, team2, score1, score2 FROM match WHERE season = ${season} ORDER BY match_date DESC"
      .as[Match]
  )
}

val readScore: ZIO[ZConnectionPool, Throwable, Option[Int]] = transaction {
  selectOne(sql"SELECT score1 from match".as[Int])
}

def predictEloGame(gameId: String): ZIO[ZConnectionPool, Throwable, Option[String]] = transaction {
  val id = gameId.toInt
  selectOne(
    sql"SELECT CASE WHEN elo_prob1 > elo_prob2 THEN team1 ELSE team2 END as winning_team FROM match WHERE id=${id}"
      .as[String]
  )
}

def predictRatingGame(gameId: String): ZIO[ZConnectionPool, Throwable, Option[String]] = transaction {
  val id = gameId.toInt
  selectOne(
    sql"SELECT CASE WHEN rating_prob1 > rating_prob2 THEN team1 ELSE team2 END as winning_team FROM match WHERE id=${id}"
      .as[String]
  )
}
