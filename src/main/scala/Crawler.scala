
import org.jsoup.Jsoup
import play.api.libs.json.{JsArray, JsValue, Json, Writes}

case class TagesschauData(Quelle: String, Title: String, text: String,Kategorie:String,Timestamp: String )

object Crawler {
  // Define an implicit Writes for TagesschauData
  implicit val tagesschauDataWrites: Writes[TagesschauData] = Json.writes[TagesschauData]

  def main(args: Array[String]): Unit = {
    // Send a GET request and parse the JSON response
    val response: requests.Response = requests.get("https://tagesschau.de/api2/news/")
    val jsonInput = Json.parse(response.text)

    // Extract the "news" array from the JSON
    val newsArray = (jsonInput \ "news").as[JsArray]

    if (newsArray.value.nonEmpty) {
      // Iterate over each news item and create JSON objects
      val newsObjects = newsArray.value.map { newsItem =>
        val quelle = "Tagesschau"
        val title = (newsItem \ "title").as[String]
        val text = extractText((newsItem\ "detailsweb").asOpt[String].getOrElse("Missing"))
        val kategorie = "none"
        val date = (newsItem \ "date").as[String]

        TagesschauData(quelle,title, text,kategorie, date)
      }

      // Convert the list of TagesschauData objects to a JSON array
      val jsonOutput = Json.toJson(newsObjects)

      // Convert the new JSON object to a string
      val outputJsonString = Json.stringify(jsonOutput)

      println(outputJsonString)
    } else {
      println("No news data found in the JSON response.")
    }

    //get relevant data from Json object

  }

  def extractText(url: String): String={
    val defaultString ="could not find articleBody"
    if(url=="Missing") defaultString
    else {
      val doc = Jsoup.connect(url).get()
      val scriptElement = doc.select("script[type=application/ld+json]").first()
      val scriptContent = scriptElement.html()
      val json: JsValue = Json.parse(scriptContent)
      val articleBody: Option[String] = (json \ "articleBody").asOpt[String]
      articleBody.getOrElse(defaultString)}

  }


}