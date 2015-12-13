package Client

import java.util
import javax.crypto.SecretKey

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

import scala.collection.mutable
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

  //Client function to register itself with the server.
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

  //Client function for logging into the server.
  def login() = {
    val pipeline: HttpRequest => Future[ChallengeResponse] = sendReceive ~> unmarshal[ChallengeResponse]
    val challRespFut: Future[ChallengeResponse] = pipeline(Post("http://localhost:8080/login", new LoginRequest(id)))

    val futLoginResp: Future[LoginResponse] = getFutureLoginResponse(challRespFut)
    futLoginResp onComplete {
      case Success(loginResp) => {
        if(loginResp.success == 1) {
          println(name + ": I logged in successfully!")
          session = loginResp.sessionToken
        }
      }
    }

  }

  //Helper function for login
  def getFutureLoginResponse(challRespFut: Future[ChallengeResponse]): Future[LoginResponse] = {
    val futLoginResp: Future[LoginResponse] = challRespFut.flatMap{
      case ChallengeResponse(challenge: Array[Byte]) => {
        val signedChallenge = rsa.sign(privateKey,challenge)
        val pipeline: HttpRequest => Future[LoginResponse] = sendReceive ~> unmarshal[LoginResponse]
        pipeline(Post("http://localhost:8080/login", new SignedChallenge(id, signedChallenge)))
      }
    }
    return futLoginResp
  }

  //Adds a random friend
  def addRandomFriend() = {
    //val randFriendName = "Bob0"
    val randFriendName = getRandomName()

    val futPubKeyMsg = getPublicKeyOf(randFriendName)
    val f = futPubKeyMsg flatMap(pubKeyMsg => addSelfToPendingOfFriend(pubKeyMsg))
    f onComplete{
      case Success(r) => { println("Added a random friend!" + r) }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }

  }

  //Helper function for adding a friend
  def getPublicKeyOf(nameOfFriend: String): Future[PublicKeyMsg] = {
    println("GETTING A PUB KEY")
    val pipeline: HttpRequest => Future[PublicKeyMsg] = sendReceive ~> unmarshal[PublicKeyMsg]
    pipeline(Get("http://localhost:8080/key", new GetPublicKey(id, session, nameOfFriend)))
  }

  //Helper function for adding a friend.
  def addSelfToPendingOfFriend(pubKeyMsg: PublicKeyMsg): Future[String] = {

    println("ADDING SELF TO FRIEND")
    val publicKeyOfFriend = rsa.decodePublicKey(pubKeyMsg.publicKeyEncoded)
    val aesKeyEncrypted = rsa.encrypt(publicKeyOfFriend, aeskey.getEncoded)
    val selfCard = Friend(id,name,publicKey.getEncoded,aesKeyEncrypted)
    val friendId = pubKeyMsg.id

    val pipeline: HttpRequest => Future[String] = sendReceive ~> unmarshal[String]
    pipeline(Post("http://localhost:8080/addPendingFriend", new AddPendingFriend(id, session, friendId, selfCard)))

  }

  def printPendingFriendList() = {
    val futPendingFriendList = getPendingFriendsList()
    futPendingFriendList onComplete{
      case Success(pfl) => { println(name + " :: " + pfl.pendingFriends) }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }

  def getPendingFriendsList(): Future[PendingFriendsListMsg] = {
    val pipeline: HttpRequest => Future[PendingFriendsListMsg] = sendReceive ~> unmarshal[PendingFriendsListMsg]
    pipeline(Get("http://localhost:8080/pendingFriend", new GetPendingFriendsList(id, session)))
  }

  def getFriendsList(): Future[FriendsListMsg] = {
    val pipeline: HttpRequest => Future[FriendsListMsg] = sendReceive ~> unmarshal[FriendsListMsg]
    pipeline(Get("http://localhost:8080/friend", new GetFriendsList(id, session)))
  }

  def printFriendList() = {
    val futFriendList = getFriendsList()
    futFriendList onComplete{
      case Success(fl) => { println(name + " :: " + fl.friends) }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }

  def sendAcceptanceOfPendingFriends(pendingFriendsListMsg: PendingFriendsListMsg): Future[String] = {
    val pfl = pendingFriendsListMsg.pendingFriends
    var nameCardList: List[Friend] = List()

    for(i <- pfl.indices){
      val friendPubKey = rsa.decodePublicKey(pfl(i).publicKeyEncoded)
      val aesKeyEncrypted = rsa.encrypt(friendPubKey, aeskey.getEncoded)
      nameCardList = nameCardList :+ Friend(id, name, publicKey.getEncoded, aesKeyEncrypted)
    }

    val pipeline: HttpRequest => Future[String] = sendReceive ~> unmarshal[String]
    pipeline(Post("http://localhost:8080/acceptFriends", new AcceptFriends(id, session, pfl, nameCardList)))
  }

  def acceptFriends() = {

    val futPflMsg = getPendingFriendsList()
    val f = futPflMsg flatMap(pflMsg => sendAcceptanceOfPendingFriends(pflMsg))
    f onComplete{
      case Success(r) => { println("Accepted Friends!" + r) }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }

  }

  def postOnOwnPage() = {
    val msg: String = "Post number " + postNumber + " from " + name + "."
    val msgEncrypted = aes.encrypt(aeskey,msg.getBytes)

    val pipeline: HttpRequest => Future[String] = sendReceive ~> unmarshal[String]
    val f: Future[String] = pipeline(Post("http://localhost:8080/post",
      new NewPost(id,session,id,FbPost(msgEncrypted,name))))
    f onComplete {
      case Success(r) => { println(name + ": I posted on my own wall!") }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    postNumber = postNumber + 1
  }

  def postOnFriendPage(friendCard: Friend){

    val idOfPage = friendCard.id
    val aesKeyOfPageEncoded = rsa.decrypt(privateKey,friendCard.aesKeyEncrypted)
    val aesKeyOfPage = aes.decodeSecretKey(aesKeyOfPageEncoded)

    val msg: String = "Post number " + postNumber + " from " + name + "."
    val msgEncrypted = aes.encrypt(aesKeyOfPage,msg.getBytes)

    val pipeline: HttpRequest => Future[String] = sendReceive ~> unmarshal[String]
    val f: Future[String] = pipeline(Post("http://localhost:8080/post",
      new NewPost(id,session,idOfPage,FbPost(msgEncrypted,name))))
    f onComplete {
      case Success(r) => { println(name + ": I posted on " + friendCard.name + "'s wall!") }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    postNumber = postNumber + 1

  }

  def readOwnPage() = {
    val fOwnPageMsg: Future[PageMsg] = getPage(id)
    fOwnPageMsg onComplete{
      case Success(ownPageMsg) => {
        println(name + ": Reading my own wall!")
        if(ownPageMsg.fbPosts.nonEmpty) {
          val mostRecentPost = ownPageMsg.fbPosts.last
          val decryptedMsg: String = new String(aes.decrypt(aeskey, mostRecentPost.encryptedMessage))
          println(name + ": My most recent post was from " + mostRecentPost.posterName + " and it said - " + decryptedMsg)
        }
        else{
          println(name + ": There are no posts on my wall.")
        }
      }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }

  def getPage(idOfPage: Int): Future[PageMsg] = {
    val pipeline: HttpRequest => Future[PageMsg] = sendReceive ~> unmarshal[PageMsg]
    pipeline(Get("http://localhost:8080/page", new GetPage(id,session,idOfPage)))
  }

  //Posts on a random friends page.
  def postOnRandomFriendPage() = {
    val fl: Future[FriendsListMsg] = getFriendsList()
    fl onComplete {
      case Success(flmsg) => {

        val friends = flmsg.friends
        val randFriend = friends(RNG.getRandNum(friends.length))
        postOnFriendPage(randFriend)

      }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }

  def updateProfile() = {
//    val birthday = "11/12/1992"
//    val relationship = "In a Relationship"
//    val pic = new Picture(Array.fill[Byte](1024)(1))
//    val newPro: Profile = new Profile(name, birthday, pic, relationship)
//
//    val pipeline: HttpRequest => Future[String] = sendReceive ~> unmarshal[String]
//    val f: Future[String] = pipeline(Post("http://localhost:8080/profile", SetProfile(id, newPro)))
//    f onComplete {
//      case Success(r) => { if(shouldPrint) println(name + ": Updated my profile!") }
//      case Failure(t) => println("An error has occured: " + t.getMessage)
//    }
  }

  def postPicture() = {

//    val newPicture = new Picture(Array.fill[Byte](1024)(2))
//
//    val pipeline: HttpRequest => Future[String] = sendReceive ~> unmarshal[String]
//    val f: Future[String] = pipeline(Post("http://localhost:8080/album", NewPicture(id, newPicture)))
//    f onComplete {
//      case Success(r) => { if(shouldPrint) println(name + ": Posted a new picture in my album!") }
//      case Failure(t) => println("An error has occured: " + t.getMessage)
//    }
  }

  def readRandomFriendPage() = {
//    val fl: Future[FriendsListMsg] = getFriendsList()
//    fl onComplete {
//      case Success(flmsg) => {
//
//        val friends = flmsg.friends
//
//        if(friends.nonEmpty) {
//
//          val randFriend = friends(RNG.getRandNum(friends.length))
//          val randFriendID = randFriend.id
//          val randFriendName = randFriend.name
//
//          val pipeline: HttpRequest => Future[PageMsg] = sendReceive ~> unmarshal[PageMsg]
//          val f: Future[PageMsg] = pipeline(Get("http://localhost:8080/page", GetPage(randFriendID)))
//          f onComplete {
//            case Success(pageMsg) => {
//              if(shouldPrint) {
//                println(name + ": Reading " + randFriendName + "'s page.")
//                if (pageMsg.fbPosts.nonEmpty) {
//                  println("Their most recent post was -- " + pageMsg.fbPosts.last)
//                }
//              }
//            }
//            case Failure(t) => println("An error has occured: " + t.getMessage)
//          }
//
//        }
//
//      }
//      case Failure(t) => println("An error has occured: " + t.getMessage)
//    }
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

  context.system.scheduler.scheduleOnce((4000+delayMillis) millisecond) {
    addRandomFriend()
  }

  context.system.scheduler.scheduleOnce((6000+delayMillis) millisecond) {
    printPendingFriendList()
  }

  context.system.scheduler.scheduleOnce((8000+delayMillis) millisecond) {
    acceptFriends()
  }

  context.system.scheduler.scheduleOnce((10000+delayMillis) millisecond) {
    printFriendList()
  }

  context.system.scheduler.scheduleOnce((12000+delayMillis) millisecond) {
    postOnRandomFriendPage()
  }

  context.system.scheduler.scheduleOnce((14000+delayMillis) millisecond) {
    readOwnPage()
  }
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
