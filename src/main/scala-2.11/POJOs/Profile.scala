package POJOs

class Profile(name_p: String, birthday_p: String, profilePic_p: Picture) {
  var name: String = name_p
  var birthday: String = birthday_p
  var profilePic: Picture = profilePic_p
//  var status: String = "Single"

  def this(name_p: String) {
    this(name_p, "", new Picture())
  }

  override def toString() : String = {
    val myString = "Name:" + name + "\n" + "Birthday: " + birthday + "\n" + " PIC: " + profilePic
//    + "Relationship Status: " + status
    return myString
  }

}
