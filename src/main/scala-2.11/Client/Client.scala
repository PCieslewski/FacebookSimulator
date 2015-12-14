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

import spray.json
import spray.json._
import CustomJsonProtocol._




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

  val delayBetweenActions = 2000
  val delay = 0


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
          if(shouldPrint) println(name + ": I logged in successfully!")
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
      case Success(r) => { if(shouldPrint) println(name + ": Sent friend request to " + randFriendName) }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }

  }

  //Helper function for adding a friend
  def getPublicKeyOf(nameOfFriend: String): Future[PublicKeyMsg] = {
    val pipeline: HttpRequest => Future[PublicKeyMsg] = sendReceive ~> unmarshal[PublicKeyMsg]
    pipeline(Get("http://localhost:8080/key", new GetPublicKey(id, session, nameOfFriend)))
  }

  //Helper function for adding a friend.
  def addSelfToPendingOfFriend(pubKeyMsg: PublicKeyMsg): Future[String] = {

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
      case Success(pfl) => { if(shouldPrint) println(name + " :: " + pfl.pendingFriends) }
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
      case Success(fl) => { if(shouldPrint) println(name + " :: " + fl.friends) }
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
      case Success(r) => { if(shouldPrint) println(name + ": Accepted all pending friends!") }
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
      case Success(r) => { if(shouldPrint) println(name + ": I posted on my own wall!") }
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
      case Success(r) => { if(shouldPrint) println(name + ": I posted on " + friendCard.name + "'s wall!") }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    postNumber = postNumber + 1

  }

  def readOwnPage() = {
    val fOwnPageMsg: Future[PageMsg] = getPage(id)
    fOwnPageMsg onComplete{
      case Success(ownPageMsg) => {
        if(shouldPrint) println(name + ": Reading my own wall!")
        if(ownPageMsg.fbPosts.nonEmpty) {
          val mostRecentPost = ownPageMsg.fbPosts.last
          val decryptedMsg: String = new String(aes.decrypt(aeskey, mostRecentPost.encryptedMessage))
          if(shouldPrint) println(name + ": My most recent post was from " + mostRecentPost.posterName + " and it said '" + decryptedMsg + "'")
        }
        else{
          if(shouldPrint) println(name + ": There are no posts on my wall.")
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

        if(friends.length > 0) {
          val randFriend = friends(RNG.getRandNum(friends.length))
          postOnFriendPage(randFriend)
        }

      }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }

  def updateProfile() = {
    val birthday = "11/12/1992"
    val relationship = "In a Relationship"
    val pic = new Picture(Array.fill[Byte](10)(1))
    val newPro: Profile = new Profile(name, birthday, pic, relationship)
    val newProJson = newPro.toJson
    val newProEncrypted = aes.encrypt(aeskey, newProJson.prettyPrint.getBytes)

    val pipeline: HttpRequest => Future[String] = sendReceive ~> unmarshal[String]
    val f: Future[String] = pipeline(Post("http://localhost:8080/profile", SetProfile(id, session, newProEncrypted)))
    f onComplete {
      case Success(r) => { if(shouldPrint) println(name + ": Updated my profile!") }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }

  def postPicture() = {

    val newPicture = new Picture(aes.encrypt(aeskey,Array.fill[Byte](10)(2)))

    val pipeline: HttpRequest => Future[String] = sendReceive ~> unmarshal[String]
    val f: Future[String] = pipeline(Post("http://localhost:8080/album", NewPicture(id, session, newPicture)))
    f onComplete {
      case Success(r) => { if(shouldPrint) println(name + ": Posted a new picture in my album!") }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }

  }

  def readRandomFriendPage() = {

    var friendAesKey: SecretKey = null
    var randFriend: Friend = null

    val futfl = getFriendsList()
    val futpage = futfl.flatMap{
      fl => {
          if(fl.friends.length <= 0) throw new Exception("No friends!")
          randFriend = fl.friends(RNG.getRandNum(fl.friends.length))
          friendAesKey = aes.decodeSecretKey(rsa.decrypt(privateKey, randFriend.aesKeyEncrypted))
          getPage(randFriend.id)
      }
    }

    futpage onComplete {
      case Success(friendPage) => {

        if(shouldPrint) println(name + ": Reading "+ randFriend.name +"'s page...")

        //Read the posts on the page.
        if(friendPage.fbPosts.nonEmpty) {
          val mostRecentPost = friendPage.fbPosts.last
          val decryptedMsg: String = new String(aes.decrypt(friendAesKey, mostRecentPost.encryptedMessage))
          if(shouldPrint) println(name + ": The most recent post on "+ randFriend.name +"'s page was from " + mostRecentPost.posterName + " and it said '" + decryptedMsg + "'")
        }
        else{
          if(shouldPrint) println(name + ": There are no posts on "+ randFriend.name +"'s page.")
        }

        //Check the status on the page.
        if(friendPage.profileEncrypted.nonEmpty){
          val friendProfileJson = new String(aes.decrypt(friendAesKey,friendPage.profileEncrypted)).parseJson
          val friendProfile = friendProfileJson.convertTo[Profile]
          if(shouldPrint) println(name + ": " + randFriend.name +"'s has a relationship status of: " + friendProfile.relationship)
        }

        //Look at the most recent photo of the page.
        if(friendPage.album.nonEmpty) {
          val mostRecentPhotoEncrypted = friendPage.album.last
          val mostRecentPhoto = Picture(aes.decrypt(friendAesKey, mostRecentPhotoEncrypted.bytes))
          if (shouldPrint) println(name + ": " + randFriend.name + "'s last photo: " + mostRecentPhoto.toJson.prettyPrint)
        }
        else{
          if (shouldPrint) println(name + ": " + randFriend.name + "'s album is empty.")
        }


      }
      case Failure(t) => ()
    }
  }

  def generatePrivateMessage(): String = {
    val num = RNG.getRandNum(8)
    num match{
      case 0 => "Hey"
      case 1 => "How's life?"
      case 2 => "What's up"
      case 3 => "Howdy"
      case 4 => "We should catch up sometime!"
      case 5 => "Did you see the game last night?"
      case 6 => "Why are the Steelers so good?"
      case 7 => "When do you want to study?"
    }
  }

  def sendPrivateMessage()  = {
    val choice = generatePrivateMessage()

    val futFriendList = getFriendsList()

    futFriendList onComplete {
      case Success(friendListMsg) => {
        if (friendListMsg.friends.size > 0){
          val num = RNG.getRandNum(friendListMsg.friends.size)
          val thatFriend = friendListMsg.friends(num)
          val friendsPublicKey = rsa.decodePublicKey(thatFriend.publicKeyEncoded)
          val encryptedMessage = rsa.encrypt(friendsPublicKey, choice.getBytes())

          val pipeline: HttpRequest => Future[String] = sendReceive ~> unmarshal[String]
          pipeline(Post("http://localhost:8080/sendPrivateMessage", new NewPrivateMessage(id, session, thatFriend.id, FbMessage(encryptedMessage, name))))


          if (shouldPrint) println(name + ": sent the private message: '" + choice + "' to " + thatFriend.name)
        }
      }
      case Failure(t) => { println("An error has occured: " + t.getMessage) }
    }
  }

  def readPrivateMessages()  = {
    val futureOwnMessages: Future[PageMsg] = getPage(id)

    futureOwnMessages onComplete {
      case Success(ownMessages) => {
        if(ownMessages.fbMessages.size == 0) {
          if(shouldPrint) println(name + ": I don't have any private messages yet!")
        }
        else {
          val mostRecentPrivateMessage = ownMessages.fbMessages.last
          val decryptMessage = new String(rsa.decrypt(privateKey, mostRecentPrivateMessage.encryptedMessage))
          if(shouldPrint) println(name + ": My most recent private message was from: " + mostRecentPrivateMessage.sender + " and it said: '" + decryptMessage + "'")
        }
      }
      case Failure(r) => {
        println("Failed to read private messages")
      }
    }

  }

  def takeAction() = {
    val num = RNG.getRandNum(10)
    num match{
      case 0 => addRandomFriend()
      case 1 => acceptFriends()
      case 2 => postOnOwnPage()
      case 3 => postOnRandomFriendPage()
      case 4 => postPicture()
      case 5 => updateProfile()
      case 6 => readRandomFriendPage()
      case 7 => sendPrivateMessage()
      case 8 => readPrivateMessages()
      case 9 => readOwnPage()

    }
    context.system.scheduler.scheduleOnce(delayBetweenActions millisecond, self, TakeAction())
  }



  //registerSelf()
  context.system.scheduler.scheduleOnce(delayMillis millisecond) {
    registerSelf()
  }

  context.system.scheduler.scheduleOnce((delayBetweenActions*2+delayMillis+totalBobs) millisecond) {
    login()
  }

  context.system.scheduler.scheduleOnce((delayBetweenActions*4+delayMillis+totalBobs) millisecond) {
    addRandomFriend()
  }

  context.system.scheduler.scheduleOnce((delayBetweenActions*6+delayMillis+totalBobs) millisecond) {
    acceptFriends()
  }

//  context.system.scheduler.scheduleOnce((10000+delayMillis) millisecond) {
//    printFriendList()
//  }

  context.system.scheduler.scheduleOnce((delayBetweenActions*8+delayMillis+totalBobs) millisecond) {
    postOnRandomFriendPage()
  }

  context.system.scheduler.scheduleOnce((delayBetweenActions*9+delayMillis+totalBobs) millisecond) {
    updateProfile()
  }

  context.system.scheduler.scheduleOnce((delayBetweenActions*10+delayMillis+totalBobs) millisecond) {
    postPicture()
  }

  context.system.scheduler.scheduleOnce((delayBetweenActions*11+delayMillis+totalBobs) millisecond) {
    readRandomFriendPage()
  }

  context.system.scheduler.scheduleOnce((delayBetweenActions*12+delayMillis+totalBobs) millisecond) {
    self ! new TakeAction()
  }

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
