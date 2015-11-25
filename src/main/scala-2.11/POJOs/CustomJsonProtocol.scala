package POJOs

import spray.json._

object CustomJsonProtocol extends DefaultJsonProtocol{

  implicit object TextJsonFormat extends RootJsonFormat[Text] {
    def write(t: Text) = JsObject(
      "message" -> JsString(t.message),
      "poster" -> JsString(t.poster)
    )
    def read(value: JsValue) = {
      value.asJsObject.getFields("message","poster") match {
        case Seq(JsString(message), JsString(poster)) =>
          new Text(message,poster)
        case _ => throw new DeserializationException("Bad Text Json Object.")
      }
    }
  }

}
