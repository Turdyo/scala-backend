package mlb

import java.io.File
import zio.jdbc._
import com.github.tototoshi.csv._
import zio._

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