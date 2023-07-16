package mlb

import java.io.File
import zio._
import zio.jdbc._
import zio.http.Response
import com.github.tototoshi.csv._
import scala.collection.mutable

type Data = List[List[Option[String]]]

def csvToList(path: String): Data = {
  val reader = CSVReader.open(new File(path))

  return reader
    .all()
    .zipWithIndex
    .filter((line, index) => index != 0 && line.length == 26)
    .map((line, index) =>
      line.map(value => {
        if (value == "") None
        else Option(value)
      })
    )
}

def optionToJson(optionnalValue: Option[Any]): Response = {
  Response.json(s"""{"response": ${optionnalValue.getOrElse(default = null)}}""")
}

def chunkOfTwoToJson(chunk: Chunk[(Any, Any)]): Response = chunk.isEmpty match
  case false => {
    val stringBuilder = mutable.StringBuilder("[")
    chunk.toList.foreach((a, b) => stringBuilder.addAll(s"""{"val1":${a}, "val2":${b}},"""))
    stringBuilder.replace(start = stringBuilder.length() - 1, end = stringBuilder.length(), "").addOne(']')
    Response.json(s"""{"response": ${stringBuilder.toString()}}""")
  }
  case true => {
    Response.json(s"""{"response": ${null}}""")
  }

/**
  * Transform a chunk of matches into a json response
  *
  * @param matchChunk the chunk of matches
  * @return Json API response
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

/**
  *  Transform a match option to JSON response
  *
  * @param matchOption the match to be returned
  * @return Json API response
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


def optionPredictToJson(predict: Option[(String, String, String)], result: Int): Response = {
  val predictData = predict.getOrElse(null)
  predictData match
    case null => {
      Response.json(s"""{"response": ${null}}""")
    }
    case _ => {
      Response.json(s"""
        {
          "date": "${predictData(0)}",
          "team1_elo": ${predictData(1)},
          "team2_elo": ${predictData(2)},
          "Probable_winner": ${result}
        }
      """)
    }
}
