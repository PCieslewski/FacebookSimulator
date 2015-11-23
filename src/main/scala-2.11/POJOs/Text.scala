package POJOs

class Text(message_p: String, poster_p: String) {

  val message: String = message_p
  val poster: String = poster_p

  override def toString() : String = {
    val myString = "Message: " + message + "\nPerson who posted: " + poster
    return myString
  }
}
