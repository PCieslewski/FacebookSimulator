package Testing

import POJOs._
import spray.json._
import DefaultJsonProtocol._

object TestJson{

  def main(args: Array[String]) {

    import CustomJsonProtocol._

    val myText = new Text("HelloWorld","PDizzle")

    val j = myText.toJson

    val myText2 = j.convertTo[Text]

    println(myText2)

  }
}
