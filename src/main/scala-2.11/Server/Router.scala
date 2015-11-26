package Server

import POJOs._
import akka.actor._
import akka.io.IO
import spray.can.Http
import spray.http.HttpMethods._
import spray.http.{Uri, HttpRequest, HttpResponse}
import spray.routing.SimpleRoutingApp
import spray.json._
import spray.httpx.SprayJsonSupport._
import akka.pattern.ask
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import scala.util._
import akka.util.Timeout
import scala.concurrent.duration._

import scala.concurrent.Future

object Router extends App with SimpleRoutingApp{

  import CustomJsonProtocol._
  implicit val system = ActorSystem("my-system")
  implicit val timeout = Timeout(5 seconds)

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
              val fResponse: Future[RegisterResponse] = (Backend.registrar ? regReq).mapTo[RegisterResponse]
              onComplete(fResponse){
                case Success(regResp: RegisterResponse) => complete(regResp)
                case Failure(t) => complete("Cannot Register: "+t)
              }
            }
          }
        }
      }
    }~
    path("friend"){
      post {
        decompressRequest() {
          entity(as[AddFriend]) { addFri =>
            detach() {
              val fResponse: Future[String] = (Backend.friender ? addFri).mapTo[String]
              onComplete(fResponse){
                case Success(resp: String) => complete(resp)
                case Failure(t) => complete("Cannot add friend: "+t)
              }
            }
          }
        }
      }
    }~
    path("friend"){
      get {
        decompressRequest() {
          entity(as[GetFriendsList]) { getFri =>
            detach() {
              val fl = Backend.pages(getFri.requesterID).friendsList.friends
              complete{
                FriendsListMsg(fl)
              }
            }
          }
        }
      }
    }




  }

}
