package POJOs

import spray.json._

object CustomJsonProtocol extends DefaultJsonProtocol{

  //The jsonFormat# signifies number of inputs into the case class.
  implicit val TestMsgFormat = jsonFormat2(TestMsg)

  //Primitives
  implicit val FriendFormat = jsonFormat4(Friend)
  implicit val FbPostFormat = jsonFormat2(FbPost)
  implicit val PictureFormat = jsonFormat1(Picture)
  implicit val ProfileFormat = jsonFormat4(Profile)

  implicit val RegisterRequestFormat = jsonFormat2(RegisterRequest)
  implicit val RegisterResponseFormat = jsonFormat1(RegisterResponse)

  //Login messages
  implicit val LoginRequestFormat = jsonFormat1(LoginRequest)
  implicit val ChallengeResponseFormat = jsonFormat1(ChallengeResponse)
  implicit val SignedChallengeFormat = jsonFormat2(SignedChallenge)
  implicit val LoginResponseFormat = jsonFormat2(LoginResponse)

  //Friend messages
  implicit val GetPublicKeyFormat = jsonFormat3(GetPublicKey)
  implicit val PublicKeyMsgFormat = jsonFormat2(PublicKeyMsg)
  implicit val AddPendingFriendFormat = jsonFormat4(AddPendingFriend)
  implicit val AcceptFriendsFormat = jsonFormat4(AcceptFriends)
  implicit val GetPendingFriendsListFormat = jsonFormat2(GetPendingFriendsList)
  //implicit val AddFriendFormat = jsonFormat3(AddFriend)
  implicit val GetFriendsListFormat = jsonFormat2(GetFriendsList)
  implicit val FriendsListMsgFormat = jsonFormat1(FriendsListMsg)
  implicit val PendingFriendsListMsgFormat = jsonFormat1(PendingFriendsListMsg)

  implicit val NewPostFormat = jsonFormat2(NewPost)

  implicit val GetProfileFormat = jsonFormat1(GetProfile)
  implicit val SetProfileFormat = jsonFormat2(SetProfile)

  implicit val NewPictureFormat = jsonFormat2(NewPicture)

  implicit val GetPageFormat = jsonFormat1(GetPage)
  implicit val PageMsgFormat = jsonFormat4(PageMsg)

}
