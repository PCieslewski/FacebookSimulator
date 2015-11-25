package POJOs

import spray.json._

class Text(message_p: String, poster_p: String) extends DefaultJsonProtocol {

  val message: String = message_p
  val poster: String = poster_p

  override def toString() : String = {
    val myString = "Message: " + message + "\nPerson who posted: " + poster
    return myString
  }


  import DefaultJsonProtocol._
//object MyJsonProtocol extends DefaultJsonProtocol {

//  implicit object TextJsonFormat extends RootJsonFormat[Text] {
//    def write(text: Text) =
//      JsArray(JsString(text.message), JsString(text.poster))
//
//    def read(value: JsValue) = value match {
//      case JsArray(Vector(JsString(message), JsString(poster))) =>
//        new Text(message, poster)
//      case _ => deserializationError("Text expected")
//    }
//  }

//}


}

