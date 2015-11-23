package POJOs

class Friend(id_p: Int, name_p: String) {
  var id: Int = id_p
  var name: String = name_p

  override def toString() : String = {
    val myString: String = "ID: " + id + "\nname: " + name
    return myString
  }
}
