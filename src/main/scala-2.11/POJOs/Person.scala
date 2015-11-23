package POJOs

class Person(id_p: Int, name: String) {
  var page: Page = new Page(name)
  var id: Int = id_p
  var friendList: FriendsList = new FriendsList()

  override def toString() : String = {
    val myString = "Page: " + page + "\n" + "ID: " + id + "\n" + "Friends list: " + friendList
    return myString
  }
}
