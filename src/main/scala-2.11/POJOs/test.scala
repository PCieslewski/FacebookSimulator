package POJOs

import spray.json._

//import spray.json._

object test{

  def main(args: Array[String]) {
    val myPro: Profile = new Profile("will")
    myPro.birthday = "june"
    myPro.name = "will"
    myPro.status = "Single"

    println(myPro)

    val will: Friend = new Friend(0,"Pawel")
    will.id = 3
    will.name = "livesey"

    println(will)
    val test = new Page("Pawel")

    println(test.profile)

    //somehow import from Text


//    import MyJsonProtocol
//    import POJOs.MyJsonProtocol
    import POJOs.Text

//    import DefaultJsonProtocol._

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
      def write(picture: Picture) =
        JsArray(JsString(picture.picture))

      def read(value: JsValue) = value match {
        case JsArray(Vector(JsString(picture))) =>
          new Picture(picture)
        case _ => deserializationError("Picture expected")
      }
    }

//    implicit object ProfileJsonFormat extends RootJsonFormat[Profile] {
//      def write(profile: Profile) =
//        JsArray(JsString(profile.name), JsString(profile.birthday), Js(profile.profilePic))
//
//      def read(value: JsValue) = value match {
//        case JsArray(Vector(JsString(picture))) =>
//          new Picture(picture)
//        case _ => deserializationError("Picture expected")
//      }
//    }

    val aa = new Text("Hey", "Will").toJson
    println("Json stuff:")
    println(aa)

    val bb = aa.convertTo[Text]
    println("Back to (scala) object")
    println(bb)

    val picIn = new Picture("my pic!").toJson
    println(picIn)

    val picOut = picIn.convertTo[Picture]
    println(picOut)

  }
}
