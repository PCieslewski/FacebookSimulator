package POJOs

import spray.json._

object CustomJsonProtocol extends DefaultJsonProtocol{

  //The jsonFormat# signifies number of inputs into the case class.
  implicit val TestMsgFormat = jsonFormat2(TestMsg)

  implicit val RegisterRequestFormat = jsonFormat1(RegisterRequest)
  implicit val RegisterResponseFormat = jsonFormat1(RegisterResponse)

  //implicit val TestFormat = jsonFormat1()

//  case class Thing1(str: String)
//  case class Thing2(t: Thing1)
//
//  implicit val Thing1Format = jsonFormat1(Thing1)
//  implicit val Thing2Format = jsonFormat1(Thing2)
//
//  implicit val PictureFormat = jsonFormat1(Picture)
//  implicit val AlbumFormat = jsonFormat1(Album)
//
//  implicit object TestObjectJsonFormat extends RootJsonFormat[TestObject] {
//    def write(tOb: TestObject) = JsObject(
//      "str" -> JsString(tOb.str)
//    )
//    def read(value: JsValue) = {
//      value.asJsObject.getFields("str") match {
//        case Seq(JsString(str)) =>
//          new TestObject(str)
//        case _ => throw new DeserializationException("Bad Str!")
//      }
//    }
//  }

}
