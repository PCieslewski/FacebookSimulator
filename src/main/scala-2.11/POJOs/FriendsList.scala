package POJOs

class FriendsList(friends_p: List[Friend]) {

  var friends: List[Friend] = List()

  //Empty Constructor
  def this(){
    this(List())
  }

}
