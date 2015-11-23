package POJOs

class Profile {
  var name: String = _
  var birthday: String = _
  var profilePic: Picture = _
  var status: RelationshipStatus = null

  override def toString() : String = {
    val myString = "Name:" + name + "\n" + "Birthday: " + birthday + "\n" + "Relationship Status: " + status
    return myString
  }

}
