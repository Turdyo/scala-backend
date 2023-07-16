package mlb

import java.io.File
import zio._
import zio.jdbc._
import zio.http.Response
import com.github.tototoshi.csv._
import scala.collection.mutable

/**
  * Takes the path of a csv file containing the columns declared in our "match sql table". 
  * If the file is not found, it will return an empty List.
  *
  * @param path the path of the file
  * @return the List of lines, lines being either a String or None.
  */
def csvToList(path: String): List[List[Option[String]]]  = {
    val reader = CSVReader.open(new File(path))

    return reader.all().zipWithIndex
      .filter((line, index) => index != 0 && line.length == 26)
      .map((line, index) => line.map(value => {
        if(value == "") None
        else Option(value)
      }))
}

/**
  * Turns a Option of String into a valid JSON response.
  *
  * @param optionnalValue the value to turn into JSON
  * @return the Response with the newly created JSON.
  */
def optionToJson(optionnalValue: Option[Any]): Response = {
  Response.json(s"""{"response": ${optionnalValue.getOrElse(default = null)}}""")
}

def chunkOfTwoToJson(chunk: Chunk[(Any, Any)]): Response = chunk.isEmpty match
  case false => {
    val stringBuilder = mutable.StringBuilder("[")
    chunk.toList.foreach((a, b) => stringBuilder.addAll(s"""{"val1":${a}, "val2":${b}},"""))
    stringBuilder.replace(start = stringBuilder.length()-1, end = stringBuilder.length(), "").addOne(']')
    Response.json(s"""{"response": ${stringBuilder.toString()}}""")
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
