package POJOs

import spray.json._

class Text(message_p: String, poster_p: String) extends DefaultJsonProtocol {

  val message: String = message_p
  val poster: String = poster_p

  override def toString() : String = {
    val myString = "Message: " + message + "\nPerson who posted: " + poster
    return myString
  }

}

