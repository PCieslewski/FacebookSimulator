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
    val randFriendName = "Bob0"

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

  def readOwnPage() = {
    val fOwnPageMsg: Future[PageMsg] = getPage(id)
    fOwnPageMsg onComplete{
      case Success(ownPageMsg) => {
        println(name + ": Reading my own wall!")
        val mostRecentPost = ownPageMsg.fbPosts.last
        val decryptedMsg: String = new String(aes.decrypt(aeskey, mostRecentPost.encryptedMessage))
        println(name + ": My most recent post was from " + mostRecentPost.posterName + " and it said- " + decryptedMsg)
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
//    val fl: Future[FriendsListMsg] = getFriendsList()
//    fl onComplete {
//      case Success(flmsg) => {
//
//        val friends = flmsg.friends
//        val randFriend = friends(RNG.getRandNum(friends.length))
//        val randFriendID = randFriend.id
//        val randFriendName = randFriend.name
//
//        val pipeline: HttpRequest => Future[String] = sendReceive ~> unmarshal[String]
//        val f: Future[String] = pipeline(Post("http://localhost:8080/post",
//          new NewPost(randFriendID,FbPost("Post number " + postNumber + " from " + name + ".",name))))
//        postNumber = postNumber + 1
//        if(shouldPrint) {
//          println(name + ": I posted on " + randFriendName + "'s wall.")
//        }
//      }
//      case Failure(t) => println("An error has occured: " + t.getMessage)
//    }
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

  //method to get their public key
  def senderHelper(friendListMsg: FriendsListMsg): Future[String] = Future {
    println("~~~~~~~~~~~~~~~~~")
    val num = RNG.getRandNum(friendListMsg.friends.size)
    val thatFriend = friendListMsg.friends(num).name

    println("Friend: " + thatFriend)

    val futurePubKey: Future[PublicKeyMsg] = getPublicKeyOf(thatFriend)
    val f = futurePubKey flatMap(pubKeyMsg => senderHelperHelper(pubKeyMsg))

//    val futFriendList = getFriendsList()
//    val f = futFriendList flatMap(friendListMsg => senderHelper(friendListMsg))


//    futurePubKey onComplete {
//      case Success(r) => {
//        println("THEIR PUB KEY :" + futurePubKey)
//        println("decoded? :" + rsa.decodePublicKey(futurePubKey.publicKeyEncoded))
//        println("~~~~~~~~~~~~~~~~~")
//      }
//      case Failure(t) => {
//        println("Failed in senderHelper")
//      }
//    }

    temp() //makes return future[String]
  }

  def senderHelperHelper(pubKeyMsg: PublicKeyMsg): Future[String] = Future {
    val publicKeyOfFriend = rsa.decodePublicKey(pubKeyMsg.publicKeyEncoded)

    println("pub key: " + publicKeyOfFriend)

    temp()
  }

  def temp(): String = {
    return "hey"
  }

  def generatePrivateMessage(): String = {
    val num = RNG.getRandNum(8)
    num match{
      case 0 => "Hey"
      case 1 => "How's life?"
      case 2 => "What's up"
      case 3 => "Howdy"
      case 4 => "We should catch up sometime!"
      case 5 => "Did you see the game last night"
      case 6 => "Why are the Steelers so good?"
      case 7 => "When do you want to study?"
    }
  }

  def sendPrivateMessage()  = {
    println("sending private message!")

    val choice = generatePrivateMessage()
    /*
    steps:
    1)get friends list
    2)pick random friend
    3)get their public key
    4)encrypt my message with that key
    5)add it to their postList
     */

    val futFriendList = getFriendsList()
                                                      // VV get public key
//    val f = futFriendList flatMap(friendListMsg => senderHelper(friendListMsg))

    futFriendList onComplete {
      case Success(friendListMsg) => {
//        println("****************")
//        println("In on complete! Here is r: " + friendListMsg)
        val num = RNG.getRandNum(friendListMsg.friends.size)
        val thatFriend = friendListMsg.friends(num)
        val friendsPublicKey = rsa.decodePublicKey(thatFriend.publicKeyEncoded)

        val encryptedMessage = rsa.encrypt(friendsPublicKey, choice.getBytes())

        val pipeline: HttpRequest => Future[String] = sendReceive ~> unmarshal[String]
        pipeline(Post("http://localhost:8080/sendPrivateMessage", new NewPrivateMessage(id, session, thatFriend.id, FbMessage(encryptedMessage, name))))

//
//        println("friend: " + thatFriend)
//        println("Pub key encoded: " + rsa.decodePublicKey(thatFriend.publicKeyEncoded))
//
//        val friendsPublicKey = rsa.decodePublicKey(thatFriend.publicKeyEncoded)
//        val aesKeyEncrypted = rsa.encrypt(friendsPublicKey, aeskey.getEncoded) //need array of bytes
//        val friendId = thatFriend.id
////        val privateMessage = new FbMessage(choice, friendId, session, thatFriend.publicKeyEncoded, aesKeyEncrypted)
//        val privateMessage = new FbMessage(choice, friendId, session)
//
//        val pipeline: HttpRequest => Future[String] = sendReceive ~> unmarshal[String]
//        val f: Future[String] = pipeline(Post("http://localhost:8080/sendPrivateMessage", new FbMessage(choice, friendId, session)))
//
//        f onComplete{
//          case Success(r) => { println("Sent a private message!" + r) }
//          case Failure(t) => { println("ERROR: " + t) }
//        }

//        println("Friend: " + thatFriend)
        println(id + " sent a private message to " + thatFriend.id)
//        println("****************")
//        println(name + " :: " + fl.friends)
////        println("___________________________")
////        println("SIZE OF LIST: " + fl.friends.size)
////        val friendsListSize = fl.friends.size
////        val num = RNG.getRandNum(friendsListSize)
////        println("Rando Num: " + num)
//        val thatFriend = fl.friends(num)
//        println("That friend: " + thatFriend)
//        println("THEIR NAME: " + thatFriend.name) //pass into getPublicKeyOf
////        val publicKeyOfFriend = rsa.decodePublicKey(pubKeyMsg.publicKeyEncoded)
//        println("****************")
      }
      case Failure(t) => { println("An error has occured: " + t.getMessage) }
    }


//    val futPubKeyMsg = getPublicKeyOf(randFriendName)
//    val f = futPubKeyMsg flatMap(pubKeyMsg => addSelfToPendingOfFriend(pubKeyMsg))
//    f onComplete{
//      case Success(r) => { println("Added a random friend!" + r) }
//      case Failure(t) => println("An error has occured: " + t.getMessage)
//    }
  }

  def readPrivateMessages()  = {
    println("reading private message!")
    /*
    Steps:
    1)Get Private message list
    2)decrypt
    3)read most recent!
     */

//    val futureMessageList: Future[]
  }

  def takeAction() = {
    val num = RNG.getRandNum(8)
    num match{
      case 0 => addRandomFriend()
      case 1 => postOnOwnPage()
      case 2 => postOnRandomFriendPage()
      case 3 => postPicture()
      case 4 => updateProfile()
      case 5 => readRandomFriendPage()
      case 6 => sendPrivateMessage()
      case 7 => readPrivateMessages()
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
    postOnOwnPage()
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
