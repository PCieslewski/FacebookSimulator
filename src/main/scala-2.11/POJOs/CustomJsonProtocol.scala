package POJOs

import spray.json._

object CustomJsonProtocol extends DefaultJsonProtocol {


  implicit object FriendJsonFormat extends RootJsonFormat[Friend] {
    def write(friend: Friend) =
      JsArray(JsString(friend.name), JsNumber(friend.id))

    def read(value: JsValue) = value match {
      case JsArray(Vector(JsString(name), JsNumber(id))) =>
        new Friend(id.intValue(), name)

      case _ => deserializationError("Friend expected")

    }
  }



  implicit object PictureJsonFormat extends RootJsonFormat[Picture] {
    def write(pic: Picture) =
      JsArray(JsString(pic.pic))

    def read(value: JsValue) = value match {
      case JsArray(Vector(JsString(pic))) =>
        new Picture(pic)

      case _ => deserializationError("Picture expected")
    }
  }

//  implicit object FriendsListJsonFormat extends RootJsonFormat[FriendsList] {
//    def write(friendsList: FriendsList) =
////      JsArray(JsArray(friendsList.friends))
//    JsObject(
//      "Id" -> JsNumber(friendsList.friends.id),
//      "Name" -> JsString(friendsList.friends.name)
//    )
//
//    def read(value: JsValue) = value match {
//      case JsArray(Vector(SOMETHING)) =>
//        new FriendsList(value.convertTo[List[Friend]])
//
//      case _ => deserializationError("FriendsList expected")
//
//    }
//  }

  implicit object FoodListJsonFormat extends JsonFormat[Food] {

    def write(food: Food): JsValue = {
      food.slices.toJson
////      JsArray(food.slices.toArray)
//        var zz = JsArray(Vector(JsNumber(food.slices(0))))
//      var zz1 = JsArray(Vector(JsNumber(food.slices(1))))
//      zz:+JsNumber(3)
//
////      val fuck: Vector[JsNumber] = new Vector[JsNumber]()
////      println(fuck.getClass)
//
//      var arr = JsArray()
//      for (tt <- food.slices) {
//        println("STUFF: " + tt)
//        println(JsNumber(tt).getClass)
//
////        zz += JsNumber(55)
////        fuck.(tt)
////        val abc = JsNumber(tt)
////        arr += abc
//      }
//
//      var aa: JsArray = new JsArray(10)
//
//
//      var s = JsArray(JsNumber(2), JsNumber(3))
//      s += JsNumber(133).toString()
//      return s
////      JsArray(fuck)
//
    }

    def read(value: JsValue) = value match {
//      case JsArray(Vector(SOMETHING)) =>
//        new FriendsList(value.convertTo[List[Friend]])

      case _ => deserializationError("FriendsList expected")

    }
  }

  implicit object ProfileJsonFormat extends RootJsonFormat[Profile] {
    def write(profile: Profile) =
      JsObject(
        "Name" -> JsString(profile.name),
        "Birthday" -> JsString(profile.birthday),
        "Picture Object" -> JsObject("Picture name" -> JsString(profile.profilePic.pic)),
        "Relationship Status" -> JsString(profile.relationshipStatus)
      )

    def read(value: JsValue) = {
      value.asJsObject.getFields("Name", "Birthday", "Picture Object", "Relationship Status") match {
        case Seq(JsString(name), JsString(birthday), JsObject(picture), JsString(status)) =>
          new Profile(name, birthday,
            new Picture(JsObject(picture).getFields("Picture name")(0).convertTo[String]), status)

        case _ => deserializationError("Profile expected")
      }
    }
  }

  implicit object TextJsonFormat extends RootJsonFormat[Text] {
    def write(text: Text) =
      JsArray(JsString(text.message), JsString(text.poster))

    def read(value: JsValue) = value match {
      case JsArray(Vector(JsString(message), JsString(poster))) =>
        new Text(message, poster)
      case _ => deserializationError("Text expected")
    }
  }

}