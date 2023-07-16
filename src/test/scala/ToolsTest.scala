import mlb._
import zio._
import zio.http.Response

class ToolsTest extends munit.FunSuite {

  val nullResponse = Response.json("""{"response": null}""")

  test("csvToList") {
    val columnName = "date,season,neutral,playoff,team1,team2,elo1_pre,elo2_pre,elo_prob1,elo_prob2,elo1_post,elo2_post,rating1_pre,rating2_pre,pitcher1,pitcher2,pitcher1_rgs,pitcher2_rgs,pitcher1_adj,pitcher2_adj,rating_prob1,rating_prob2,rating1_post,rating2_post,score1,score2".split(",")
    val result: List[List[Option[String]]] = csvToList("./csv/mlb_elo_latest.csv")

    val correctLine: Array[Option[String]] = "2021-10-03,2021,0,,STL,CHC,1519.69110959809,1525.79717673314,0.5257286945873083,0.4742713054126917,,,1510.96575793549,1517.00028701002,,,,,,,0.5273203989501174,0.4726796010498826,,,,"
    .split(",")
    .map(value => value match
        case "" => None
        case _ => Option(value)
    )

    assert(result(0).toSeq != columnName.toSeq)
    assertEquals(result(1).toSet, correctLine.toSet)
  }

  test("optionToJson") {
    val correctResponse = Response.json("""{"response": 1}""")

    assertEquals(nullResponse, predictOptionToJson(None))
    assertEquals(correctResponse, predictOptionToJson(Option(1)))
  }
}
