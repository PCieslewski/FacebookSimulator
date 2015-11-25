package POJOs

class Post(message_p: String) {
  var message: String = message_p

  override def toString() : String = {
    val myString = "Post's message: " + message
    return myString
  }
}
