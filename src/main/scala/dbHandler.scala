package mlb

import java.io.File
import zio.jdbc._
import com.github.tototoshi.csv._
import zio._

type Data = List[List[String]] 

def insertRows(line: List[String]): ZIO[ZConnectionPool, Throwable, UpdateResult] = transaction {
    insert(
      sql"INSERT INTO baseballElo (date,season,neutral,playoff,team1,team2,elo1_pre,elo2_pre,elo_prob1,elo_prob2,elo1_post,elo2_post,rating1_pre,rating2_pre,pitcher1,pitcher2,pitcher1_rgs,pitcher2_rgs,pitcher1_adj,pitcher2_adj,rating_prob1,rating_prob2,rating1_post,rating2_post,score1,score2)".values((line))
    )
  }

def csvToList(path: String): Data = {
    val reader = CSVReader.open(new File(path))

    return reader.all().zipWithIndex
        .filter((line, index) => index != 0)
        .map((line, index) => line.map(value => {
            if(value == "") "NULL"
            else value
        }))
}