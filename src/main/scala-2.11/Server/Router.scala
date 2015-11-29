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
    }~
    path("post"){
      post {
        decompressRequest() {
          entity(as[NewPost]) { newPost =>
            detach() {
              Backend.poster ! newPost
              complete{
                "Posted."
              }
            }
          }
        }
      }
    }~
    path("profile"){
      get {
        decompressRequest() {
          entity(as[GetProfile]) { getPro =>
            detach() {
              val id = getPro.id
              complete(Backend.pages(id).profile)
            }
          }
        }
      }
    }~
    path("profile"){
      post {
        decompressRequest() {
          entity(as[SetProfile]) { setPro =>
            detach() {
              val id = setPro.id
              Backend.pages(id).profile = setPro.profile
              complete("Updated profile.")
            }
          }
        }
      }
    }~
    path("album"){
      post {
        decompressRequest() {
          entity(as[NewPicture]) { newPic =>
            detach() {
              val id = newPic.id
              Backend.pages(id).album.pictures = Backend.pages(id).album.pictures :+ newPic.picture
              complete("Added Picture.")
            }
          }
        }
      }
    }~
    path("page"){
      get {
        decompressRequest() {
          entity(as[GetPage]) { getPage =>
            detach() {
              val id = getPage.id
              val pageMsg = new PageMsg(
                Backend.pages(id).profile,
                Backend.pages(id).postsList.posts,
                Backend.pages(id).album.pictures,
                Backend.pages(id).friendsList.friends
              )
              complete(pageMsg)
            }
          }
        }
      }
    }




  }

}
