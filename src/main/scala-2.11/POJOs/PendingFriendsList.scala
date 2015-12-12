package POJOs

class PendingFriendsList(friends_p: List[Friend]) {

  var pendingFriends: List[Friend] = List()

  //Empty Constructor
  def this(){
    this(List())
  }

}
