package Server

import POJOs._
import akka.actor._
import spray.routing.SimpleRoutingApp
import spray.json._
import spray.httpx.SprayJsonSupport._

object Router extends App with SimpleRoutingApp{

  import CustomJsonProtocol._
  implicit val system = ActorSystem("my-system")

  startServer(interface = "localhost", port = 8080) {
    path("hello") {
      get {
          complete {
            val b = 6
            TestMsg(b,"FUN!!")
          }
      }
    }~
    path("register"){
      post {
        decompressRequest() {
          entity(as[RegisterRequest]) { regReq =>
            detach() {
              complete {
                val indexOfUser = Backend.registerNewUser(regReq.name)
                RegisterResponse(indexOfUser)
              }
            }
          }
        }
      }
    }
  }

}
