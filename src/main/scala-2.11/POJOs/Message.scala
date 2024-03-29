package POJOs

sealed class Message
case class TestMsg(a: Int, b: String) extends Message

//Clients send these to themselves.
case class TakeAction() extends Message

//Messages used to register a client to the server.
case class RegisterRequest(name: String) extends Message
case class RegisterResponse(id: Int) extends Message

//Friend messages
case class AddFriend(requesterID: Int, requesterName: String, friendName: String) extends Message
case class GetFriendsList(requesterID: Int) extends Message
case class FriendsListMsg(friends: List[Friend]) extends Message

//Profile messages
case class GetProfile(id: Int) extends Message
case class SetProfile(id: Int, profile: Profile) extends Message

//Album messages
case class NewPicture(id: Int, picture: Picture) extends Message

//Pages messages
case class GetPage(id: Int) extends Message
case class PageMsg(profile: Profile, fbPosts: List[FbPost], album: List[Picture], friends: List[Friend]) extends Message

//FbPost messages
case class NewPost(receiverId: Int, fbPost: FbPost) extends Message