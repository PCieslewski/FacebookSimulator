package POJOs

class Person {
  var page: Page = _
  var id: Int = _
  var friendList: FriendsList = _

  override def toString() : String = {
    val myString = "Page: " + page + "\n" + "ID: " + id + "\n" + "Friends list: " + friendList
    return myString
  }
}
