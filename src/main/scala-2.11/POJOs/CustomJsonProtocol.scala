package POJOs

import spray.json._

object CustomJsonProtocol extends DefaultJsonProtocol{

  //The jsonFormat# signifies number of inputs into the case class.
  implicit val TestMsgFormat = jsonFormat2(TestMsg)

  //Primitives
  implicit val FriendFormat = jsonFormat2(Friend)
  implicit val FbPostFormat = jsonFormat2(FbPost)
  implicit val PictureFormat = jsonFormat1(Picture)
  implicit val ProfileFormat = jsonFormat4(Profile)

  implicit val RegisterRequestFormat = jsonFormat1(RegisterRequest)
  implicit val RegisterResponseFormat = jsonFormat1(RegisterResponse)

  implicit val AddFriendFormat = jsonFormat3(AddFriend)
  implicit val GetFriendsListFormat = jsonFormat1(GetFriendsList)
  implicit val FriendsListMsgFormat = jsonFormat1(FriendsListMsg)

  implicit val NewPostFormat = jsonFormat2(NewPost)

  implicit val GetProfileFormat = jsonFormat1(GetProfile)
  implicit val SetProfileFormat = jsonFormat2(SetProfile)

  implicit val NewPictureFormat = jsonFormat2(NewPicture)

  implicit val GetPageFormat = jsonFormat1(GetPage)
  implicit val PageMsgFormat = jsonFormat4(PageMsg)

}
