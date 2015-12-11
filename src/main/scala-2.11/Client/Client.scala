package Client

import java.util

import POJOs._
import akka.actor.{ActorSystem, Actor}
import akka.pattern.ask
import akka.io.IO
import akka.util.Timeout
import spray.can.Http
import spray.httpx.unmarshalling._
import spray.httpx._
import spray.http.HttpMethods._
import spray.http.{HttpHeader, Uri, HttpRequest, HttpResponse}
import spray.json.JsObject

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import scala.concurrent.duration._

import spray.client.pipelining._
import spray.httpx.SprayJsonSupport._

import POJOs.crypto._




class Client(name_p: String, totalBobs: Int, delayMillis: Int) extends Actor {

  import context.dispatcher
  implicit var ActorSystem = context.system
  implicit val timeout: Timeout = 15.second // for the actor 'asks'
  import CustomJsonProtocol._

  //Each client knows their name.
  var id: Int = 0
  val name: String = name_p
  val baseURI = "http://localhost:8080/"
  var session = Array.empty[Byte]
  val keypair = rsa.generateKeyPair()
  val publicKey = keypair.getPublic
  val privateKey = keypair.getPrivate
  val aeskey = aes.generateSecretKey


  //Some numbers to track
  var postNumber = 0

  var shouldPrint: Boolean = false

  def getTestMsg() = {
    val pipeline: HttpRequest => Future[TestMsg] = sendReceive ~> unmarshal[TestMsg]
    val f: Future[TestMsg] = pipeline(Get("http://localhost:8080/hello"))
    f onComplete {
      case Success(r) => {
        println("I AM A GOD!")
        println(r.a)
        println(r.b)
      }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }

  }

  def registerSelf() = {
    val pipeline: HttpRequest => Future[RegisterResponse] = sendReceive ~> unmarshal[RegisterResponse]
    val f: Future[RegisterResponse] = pipeline(Post("http://localhost:8080/register", new RegisterRequest(name, publicKey.getEncoded)))
    f onComplete {
      case Success(r) => {
//        println("Registered. Got ID: " + r.id)
        id = r.id
      }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }

  def login() = {
    val pipeline: HttpRequest => Future[ChallengeResponse] = sendReceive ~> unmarshal[ChallengeResponse]
    val challRespFut: Future[ChallengeResponse] = pipeline(Post("http://localhost:8080/login", new LoginRequest(id)))

    val futLoginResp: Future[LoginResponse] = getFutureLoginResponse(challRespFut)
    futLoginResp onComplete {
      case Success(loginResp) => {
        if(loginResp.success == 1) {
          session = loginResp.sessionToken
        }
      }
    }

  }

  def getFutureLoginResponse(challRespFut: Future[ChallengeResponse]): Future[LoginResponse] = {
    val futLoginResp: Future[LoginResponse] = challRespFut.flatMap{
      case ChallengeResponse(challenge: Array[Byte]) => {
        val signedChallenge = rsa.sign(privateKey,challenge)
        val pipeline: HttpRequest => Future[LoginResponse] = sendReceive ~> unmarshal[LoginResponse]
        pipeline(Post("http://localhost:8080/login2", new SignedChallenge(id, signedChallenge)))
      }
    }
    return futLoginResp
//    val fString: Future[String] = fResponse.flatMap{
//      case (resp: HttpResponse) => Future{resp.entity.asString}
//    }
  }

  def addRandomFriend() = {
    val pipeline: HttpRequest => Future[String] = sendReceive ~> unmarshal[String]
    val randFriendName = getRandomName()
    val f: Future[String] = pipeline(Post("http://localhost:8080/friend", new AddFriend(id, name, randFriendName)))
    f onComplete {
      case Success(r) => { if(shouldPrint) println(name + ": I added " + randFriendName + " as a friend!") }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }

  def getFriendsList(): Future[FriendsListMsg] = {
    val pipeline: HttpRequest => Future[FriendsListMsg] = sendReceive ~> unmarshal[FriendsListMsg]
    return pipeline(Get("http://localhost:8080/friend", new GetFriendsList(id)))
  }

  def postOnOwnPage() = {
    val pipeline: HttpRequest => Future[String] = sendReceive ~> unmarshal[String]
    val f: Future[String] = pipeline(Post("http://localhost:8080/post",
      new NewPost(id,FbPost("Post number " + postNumber + " from " + name + ".",name))))
    f onComplete {
      case Success(r) => { if(shouldPrint) println(name + ": I posted on my own wall!") }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    postNumber = postNumber + 1
  }

  //Posts on a random friends page.
  def postOnRandomFriendPage() = {
    val fl: Future[FriendsListMsg] = getFriendsList()
    fl onComplete {
      case Success(flmsg) => {

        val friends = flmsg.friends
        val randFriend = friends(RNG.getRandNum(friends.length))
        val randFriendID = randFriend.id
        val randFriendName = randFriend.name

        val pipeline: HttpRequest => Future[String] = sendReceive ~> unmarshal[String]
        val f: Future[String] = pipeline(Post("http://localhost:8080/post",
          new NewPost(randFriendID,FbPost("Post number " + postNumber + " from " + name + ".",name))))
        postNumber = postNumber + 1
        if(shouldPrint) {
          println(name + ": I posted on " + randFriendName + "'s wall.")
        }
      }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }

  def updateProfile() = {
    val birthday = "11/12/1992"
    val relationship = "In a Relationship"
    val pic = new Picture(Array.fill[Byte](1024)(1))
    val newPro: Profile = new Profile(name, birthday, pic, relationship)

    val pipeline: HttpRequest => Future[String] = sendReceive ~> unmarshal[String]
    val f: Future[String] = pipeline(Post("http://localhost:8080/profile", SetProfile(id, newPro)))
    f onComplete {
      case Success(r) => { if(shouldPrint) println(name + ": Updated my profile!") }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }

  def postPicture() = {

    val newPicture = new Picture(Array.fill[Byte](1024)(2))

    val pipeline: HttpRequest => Future[String] = sendReceive ~> unmarshal[String]
    val f: Future[String] = pipeline(Post("http://localhost:8080/album", NewPicture(id, newPicture)))
    f onComplete {
      case Success(r) => { if(shouldPrint) println(name + ": Posted a new picture in my album!") }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }

  def readRandomFriendPage() = {
    val fl: Future[FriendsListMsg] = getFriendsList()
    fl onComplete {
      case Success(flmsg) => {

        val friends = flmsg.friends

        if(friends.nonEmpty) {

          val randFriend = friends(RNG.getRandNum(friends.length))
          val randFriendID = randFriend.id
          val randFriendName = randFriend.name

          val pipeline: HttpRequest => Future[PageMsg] = sendReceive ~> unmarshal[PageMsg]
          val f: Future[PageMsg] = pipeline(Get("http://localhost:8080/page", GetPage(randFriendID)))
          f onComplete {
            case Success(pageMsg) => {
              if(shouldPrint) {
                println(name + ": Reading " + randFriendName + "'s page.")
                if (pageMsg.fbPosts.nonEmpty) {
                  println("Their most recent post was -- " + pageMsg.fbPosts.last)
                }
              }
            }
            case Failure(t) => println("An error has occured: " + t.getMessage)
          }

        }

      }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }

  def takeAction() = {
    val num = RNG.getRandNum(6)
    num match{
      case 0 => addRandomFriend()
      case 1 => postOnOwnPage()
      case 2 => postOnRandomFriendPage()
      case 3 => postPicture()
      case 4 => updateProfile()
      case 5 => readRandomFriendPage()
    }
    context.system.scheduler.scheduleOnce(2 seconds, self, TakeAction())
  }



  //registerSelf()
  context.system.scheduler.scheduleOnce(delayMillis millisecond) {
    registerSelf()
  }

  context.system.scheduler.scheduleOnce((2000+delayMillis) millisecond) {
    login()
  }

//  context.system.scheduler.scheduleOnce((4000+delayMillis) millisecond) {
//    updateProfile()
//  }
//
//  context.system.scheduler.scheduleOnce((6000+delayMillis) millisecond) {
//    addRandomFriend()
//  }
//
//  context.system.scheduler.scheduleOnce((8000+delayMillis) millisecond) {
//    readRandomFriendPage()
//  }
//
//  context.system.scheduler.scheduleOnce((10000+delayMillis) millisecond) {
//    self ! new TakeAction()
//  }

  def receive = {
    case TakeAction() => {
      takeAction()
    }
    case "Toggle" => {
      shouldPrint = !shouldPrint
    }
  }

  def getPong: Future[String] = {
    val fResponse: Future[HttpResponse] = (IO(Http) ? HttpRequest(GET, Uri("http://localhost:8080/ping"))).mapTo[HttpResponse]
    val fString: Future[String] = fResponse.flatMap{
      case (resp: HttpResponse) => Future{resp.entity.asString}
    }
    return fString
  }

  //Generates a random name of a person that is not themselves.
  def getRandomName(): String = {
    var nameRand = "Bob"+RNG.getRandNum(totalBobs)
    while(name.equals(nameRand)){
      nameRand = "Bob"+RNG.getRandNum(totalBobs)
    }
    return nameRand
  }

  //def getFriendsList: Future[]

}
