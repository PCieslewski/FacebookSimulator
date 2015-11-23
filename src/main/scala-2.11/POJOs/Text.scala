package POJOs

class Text {
  var message: String = _
  var personWhoPosted: String = _

  override def toString() : String = {
    val myString = "Message: " + message + "\nPerson who posted: " + personWhoPosted
    return myString
  }
}
