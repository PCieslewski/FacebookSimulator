package POJOs

//class Profile(name_p: String) {
//  val name: String = name_p
//  var birthday: String = ""
//  var profilePic: Picture = new Picture(Array.fill[Byte](5)(10))
//  var status: String = "Single"
//
//  override def toString() : String = {
//    val myString = "Name:" + name + "\n" + "Birthday: " + birthday + "\n" + "Relationship Status: " + status
//    return myString
//  }
//
//}

case class Profile(name: String, birthday: String, profilePic: Picture, relationship: String)
