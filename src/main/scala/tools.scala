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

def predictOptionToJson(optionnalValue: Option[Any]): Response = {
  Response.json(s"""{"response": "${optionnalValue.getOrElse(default = null)}""}""")
}
