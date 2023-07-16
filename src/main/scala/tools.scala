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

    return reader.all().zipWithIndex
      .filter((line, index) => index != 0 && line.length == 26)
      .map((line, index) => line.map(value => {
        if(value == "") None
        else Option(value)
      }))
}

def optionToJson(optionnalValue: Option[Any]): Response = {
  Response.json(s"""{"response": ${optionnalValue.getOrElse(default = null)}}""")
}

def chunkOfTwoToJson(chunk: Chunk[(Any, Any)]):Response = {
  val stringBuilder = mutable.StringBuilder("[")
  chunk.toList.foreach((a, b) => stringBuilder.addAll(s"""{"val1":${a}, "val2":${b}},"""))
  stringBuilder.replace(start = stringBuilder.length()-1, end = stringBuilder.length(), "").addOne(']')
  Response.json(s"""{"response": ${stringBuilder.toString()}}""")
}