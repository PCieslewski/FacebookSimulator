package POJOs

import spray.json._

object CustomJsonProtocol extends DefaultJsonProtocol {

  implicit object TextJsonFormat extends RootJsonFormat[Text] {
    def write(text: Text) =
      JsArray(JsString(text.message), JsString(text.poster))

    def read(value: JsValue) = value match {
      case JsArray(Vector(JsString(message), JsString(poster))) =>
        new Text(message, poster)
      case _ => deserializationError("Text expected")
    }
  }

  implicit object PictureJsonFormat extends RootJsonFormat[Picture] {
    def write(pic: Picture) =
      JsArray(JsString(pic.pic))

    def read(value: JsValue) = value match {
      case JsArray(Vector(JsString(pic))) =>
        new Picture(pic)

//      case JsArray(Vector(JsObject(value))) =>
//        println("GET HEERE?!?!!!")
//        new Picture()
//        new Picture(pic)

//      case JsArray(Vector(JsString(null))) =>
//        new Picture()

      case _ => deserializationError("Picture expected")
        println("ERORR : " + value)
        println("CASSS " + value.getClass)
        new Picture()
    }
  }

  implicit object ProfileJsonFormat extends RootJsonFormat[Profile] {
    def write(profile: Profile) =
      JsObject(
        "Name" -> JsString(profile.name),
        "Birthday" -> JsString(profile.birthday),
        "Picture Object" -> JsObject("Picture name" -> JsString(profile.profilePic.pic))
      )

    def read(value: JsValue) = {
      value.asJsObject.getFields("Name", "Birthday", "Picture Object") match {
        case Seq(JsString(name), JsString(birthday), JsObject(picture)) =>
//          println("MY STUFF" + value.prettyPrint)
//          println("LOL?")
//          println("HERE " + value.toString())
//          val pic: Picture = JsObject(picture).convertTo[Picture]
//          println("YO  " + pic)
          println("TEST : " + JsObject(picture).getFields("Picture name")(0).convertTo[String])
//          println("WHAT " + JsObject(picture).convertTo[Picture])
          new Profile(name, birthday, new Picture(JsObject(picture).getFields("Picture name")(0).convertTo[String]))


        //      case JsArray(Vector(JsString(pic))) =>
        //        new Picture(pic)
        case _ => deserializationError("Profile expected")
      }
    }
  }

}