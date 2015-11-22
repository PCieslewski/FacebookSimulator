package POJOs

class Friend {
  var id: Int = _
  var name: String = _

  override def toString() : String = {
    val myString: String = "ID: " + id + "\nname: " + name
    return myString
  }
}
