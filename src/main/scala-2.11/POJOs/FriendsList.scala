package POJOs

class FriendsList {
  var friends: List[Friend] = _

  override def toString() : String = {
    var myString = "Friends: \n"
    var index = 0
    for(frds <- friends) {
      myString += "Friend #" + index + "\n"
      index += 1
      myString += (frds + "\n")
    }
    return myString
  }
}
