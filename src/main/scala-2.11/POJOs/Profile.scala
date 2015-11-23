package POJOs

class Profile(name_p: String) {
  var name: String = name_p
  var birthday: String = ""
  var profilePic: Picture = new Picture()
  var status: RelationshipStatus = Single()

  override def toString() : String = {
    val myString = "Name:" + name + "\n" + "Birthday: " + birthday + "\n" + "Relationship Status: " + status
    return myString
  }

}
