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
    path("login"){
      post {
        println("Got here")
        decompressRequest() {
          jsonpWithParameter("signedChallenge") {
            entity(as[SignedChallenge]) { signedChallenge =>
              detach() {
                println("VERIFYING CHALLENGE")
                val f: Future[LoginResponse] = (Backend.loginActor ? signedChallenge).mapTo[LoginResponse]
                onComplete(f) {
                  case Success(loginResp: LoginResponse) => {
                    println(new String(loginResp.sessionToken))
                    complete(loginResp)
                  }
                  case Failure(t) => complete("Failed Login Procedure. : " + t)
                }
              }
            }
          }~
          entity(as[LoginRequest]) { loginReq =>
            detach() {
              println("SENDING CHALLENGE")
              val f: Future[ChallengeResponse] = (Backend.loginActor ? loginReq).mapTo[ChallengeResponse]
              onComplete(f){
                case Success(challengeResp: ChallengeResponse) => complete(challengeResp)
                case Failure(t) => complete("Cannot get challenge: "+t)
              }
            }
          }
        }
      }
    }~
    path("key"){
      get {
        decompressRequest() {
          entity(as[GetPublicKey]) { getPubKey =>
            detach() {
              val loggedIn = Backend.verifySession(getPubKey.id, getPubKey.session)
              if(loggedIn){
                val id = Backend.getIdFromName(getPubKey.name)
                val pubKey = Backend.pages(id).publicKeyEncoded
                complete(PublicKeyMsg(id, pubKey))
              }
              else{
                println("A user failed session verification in GetPublicKey.")
                complete("Not logged in.")
              }
            }
          }
        }
      }
    }~
    path("addPendingFriend"){
      post {
        decompressRequest() {
          entity(as[AddPendingFriend]) { addPendFriend =>
            detach() {
              val fResponse: Future[String] = (Backend.friender ? addPendFriend).mapTo[String]
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
              val loggedIn = Backend.verifySession(getFri.id, getFri.session)
              if (loggedIn) {
                val fl = Backend.pages(getFri.id).friendsList.friends
                complete(FriendsListMsg(fl))
              }
              else {
                println("A user failed session verification in GetFriendsList.")
                complete("Not logged in.")
              }
            }
          }
        }
      }
    }~
    path("pendingFriend"){
      get {
        decompressRequest() {
          entity(as[GetPendingFriendsList]) { getPendFri =>
            detach() {
              val loggedIn = Backend.verifySession(getPendFri.id, getPendFri.session)
              if (loggedIn) {
                val pfl = Backend.pages(getPendFri.id).pendingFriendsList.pendingFriends
                complete(PendingFriendsListMsg(pfl))
              }
              else {
                println("A user failed session verification in GetPendingFriendsList.")
                complete("Not logged in.")
              }
            }
          }
        }
      }
    }~
    path("acceptFriends"){
      post {
        decompressRequest() {
          entity(as[AcceptFriends]) { accFri =>
            detach() {
              val loggedIn = Backend.verifySession(accFri.id, accFri.session)
              if (loggedIn) {
                val fResponse: Future[String] = (Backend.friender ? accFri).mapTo[String]
                onComplete(fResponse){
                  case Success(resp: String) => complete(resp)
                  case Failure(t) => complete("Cannot add friend: "+t)
                }
              }
              else {
                println("A user failed session verification in AcceptFriends.")
                complete("Not logged in.")
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
              val loggedIn = Backend.verifySession(newPost.id, newPost.session)
              if(loggedIn){
                val f: Future[String] = (Backend.poster ? newPost).mapTo[String]
                onComplete(f){
                  case Success(resp: String) => complete("Posted message!")
                  case Failure(t) => complete("Cannot post message. : "+t)
                }
              }
              else{
                println("A user failed session verification in NewPost.")
                complete("Not logged in.")
              }
            }
          }
        }
      }
    }~
//    path("profile"){
//      get {
//        decompressRequest() {
//          entity(as[GetProfile]) { getPro =>
//            detach() {
//              val id = getPro.id
//              complete(Backend.pages(id).profileEncrypted)
//            }
//          }
//        }
//      }
//    }~
    path("profile"){
      post {
        decompressRequest() {
          entity(as[SetProfile]) { setPro =>
            detach() {
              val loggedIn = Backend.verifySession(setPro.id, setPro.session)
              if(loggedIn) {
                val id = setPro.id
                Backend.pages(id).profileEncrypted = setPro.profileEncrypted
                complete("Updated profile.")
              }
              else{
                println("A user failed session verification in SetProfile.")
                complete("Not logged in.")
              }
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
              val loggedIn = Backend.verifySession(newPic.id, newPic.session)
              if(loggedIn) {
                val id = newPic.id
                Backend.pages(id).album.pictures = Backend.pages(id).album.pictures :+ newPic.picture
                complete("Added Picture.")
              }
              else{
                println("A user failed session verification in SetProfile.")
                complete("Not logged in.")
              }
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
              val loggedIn = Backend.verifySession(getPage.id, getPage.session)
              if(loggedIn) {
                val id = getPage.idOfPage
                val pageMsg = new PageMsg(
                  Backend.pages(id).profileEncrypted,
                  Backend.pages(id).postsList.posts,
                  Backend.pages(id).album.pictures,
                  Backend.pages(id).friendsList.friends,
                  Backend.pages(id).messageList.messages
                )
                complete(pageMsg)
              }
              else{
                println("A user failed session verification in GetPage.")
                complete("Not logged in.")
              }
            }
          }
        }
      }
    }~
    path("sendPrivateMessage"){
      post {
        decompressRequest() {
          entity(as[NewPrivateMessage]) { newPrivateMessage =>
            detach() {
              val loggedIn = Backend.verifySession(newPrivateMessage.id, newPrivateMessage.session)
              if(loggedIn){
                val f: Future[String] = (Backend.messenger ? newPrivateMessage).mapTo[String]
                onComplete(f){
                  case Success(resp: String) => complete(newPrivateMessage.id + " sent message to: " + newPrivateMessage.receiverId)
                  case Failure(t) => complete("Cannot post message. : "+t)
                }
              }
              else{
                println("A user failed session verification in NewPost.")
                complete("Not logged in.")
              }
            }
          }
        }
      }
    }

//sendPrivateMessage


  }

}
