package mlb

import java.io.File
import zio._
import zio.jdbc._
import zio.http.Response
import com.github.tototoshi.csv._
import scala.collection.mutable
import java.io.FileNotFoundException

/** Takes the path of a csv file containing the columns declared in our "match sql table". If the file is not found, it
  * will return an empty List.
  *
  * @param path
  *   the path of the file
  * @return
  *   the List of lines, lines being either a String or None.
  */
def csvToList(path: String): Option[List[List[Option[String]]]] = {

  val correctColumns = List(
    "date",
    "season",
    "neutral",
    "playoff",
    "team1",
    "team2",
    "elo1_pre",
    "elo2_pre",
    "elo_prob1",
    "elo_prob2",
    "elo1_post",
    "elo2_post",
    "rating1_pre",
    "rating2_pre",
    "pitcher1",
    "pitcher2",
    "pitcher1_rgs",
    "pitcher2_rgs",
    "pitcher1_adj",
    "pitcher2_adj",
    "rating_prob1",
    "rating_prob2",
    "rating1_post",
    "rating2_post",
    "score1",
    "score2"
  )

  try {
    val reader = CSVReader.open(new File(path))
    val csvValues = reader.all()

    if (csvValues(0) != correctColumns) {
      return None
    }

    return Option(
      csvValues
      .zipWithIndex
      .filter((line, index) => index != 0 && line.length == 26)
      .map((line, index) =>
        line.map(value => {
          if (value == "") None
          else Option(value)
        })
      ))

  } catch {
    case e: FileNotFoundException => None
  }
}

/** Transform a chunk of matches into a json response
  *
  * @param matchChunk
  *   the chunk of matches
  * @return
  *   Json API response
  */
def matchChunkToJsonReponse(matchChunk: Chunk[Match]): Response = matchChunk.isEmpty match
  case false => {
    val response = mutable.StringBuilder()
    matchChunk.foreach((id, date, season, team1, team2, score1, score2) => response.addAll(s"""{
      "id": "${id}",
      "date": "${date}",
      "season": "${season}",
      "team1": "${team1}",
      "team2": "${team2}",
      "score1": ${score1},
      "score2": ${score2}
    },"""))
    response.replace(start = response.length() - 1, end = response.length(), "")
    Response.json(s"""[${response.toString}]""")
  }
  case true => {
    Response.json(s"""{"response": ${null}}""")
  }

/** Transform a match option to JSON response
  *
  * @param matchOption
  *   the match to be returned
  * @return
  *   Json API response
  */
def matchOptionToJsonReponse(matchOption: Option[Match]): Response = matchOption.isEmpty match
  case false => {
    val matchData = matchOption.get
    val response = mutable.StringBuilder(s"""{
      "id": "${matchData._1}",
      "date": "${matchData._2}",
      "season": "${matchData._3}",
      "team1": "${matchData._4}",
      "team2": "${matchData._5}",
      "score1": ${matchData._6},
      "score2": ${matchData._7}
    }""")
    Response.json(s"""{"response": ${response.toString}}""")
  }
  case true => {
    Response.json(s"""{"response": ${null}}""")
  }

/** Turns a Option of String into a valid JSON response.
  *
  * @param optionnalValue
  *   the value to turn into JSON
  * @return
  *   the Response with the newly created JSON.
  */
def predictOptionToJson(optionnalValue: Option[Any]): Response = optionnalValue.isDefined match 
  case true => Response.json(s"""{"response": "${optionnalValue.get}"}""")
  case false => Response.json(s"""{"response": ${null}}""")
