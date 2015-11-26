package POJOs

import spray.json._

object CustomJsonProtocol extends DefaultJsonProtocol{

  //The jsonFormat# signifies number of inputs into the case class.
  implicit val TestMsgFormat = jsonFormat2(TestMsg)

  implicit val RegisterRequestFormat = jsonFormat1(RegisterRequest)
  implicit val RegisterResponseFormat = jsonFormat1(RegisterResponse)

  implicit val AddFriendFormat = jsonFormat3(AddFriend)

}
