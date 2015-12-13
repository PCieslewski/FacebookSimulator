package Server

import java.security.SecureRandom

import POJOs._
import POJOs.crypto.rsa
import akka.actor._

import scala.collection.mutable
import scala.collection.mutable._

object Backend {

  implicit val backendSystem = ActorSystem()

  var pages = new mutable.MutableList[Page]
  var sessions = new mutable.ArrayBuffer[Array[Byte]]
  val challenges = new mutable.ArrayBuffer[Array[Byte]]
  var index = 0
  val registrar = backendSystem.actorOf(Props(new Registrar()), name = "Registrar")
  val friender = backendSystem.actorOf(Props(new Friender()), name = "Friender")
  val poster = backendSystem.actorOf(Props(new Poster()), name = "Poster")
  val loginActor = backendSystem.actorOf(Props(new LoginActor()), name = "LoginActor")
  val nameIdMap: Map[String,Int] = Map()
  val messenger = backendSystem.actorOf(Props(new Messenger()), name = "Messenger")

  def registerNewUser(name: String, encodedPublicKey: Array[Byte]): Int = {
    pages += new Page(name, encodedPublicKey)
    sessions += Array.empty[Byte]
    challenges += Array.empty[Byte]

    nameIdMap += (name -> index)

    index = index + 1
    return index - 1
  }

  def getIdFromName(name: String): Int = {
//    for(i <- pages.indices){
//      if(pages(i).profile.name.equals(name)){
//        return i
//      }
//    }
//    return -1
    return nameIdMap.getOrElse(name, -1)
  }

  def verifySession(id: Int, session: Array[Byte]): Boolean = {
    if(sessions(id).sameElements(session)){
      return true
    }
    else{
      return false
    }
  }

}

class Messenger extends Actor{
  def receive = {
    case NewPrivateMessage(message: String, id: Int, session: Array[Byte], publicKeyEncoded: Array[Byte], aesKeyEncrypted: Array[Byte]) => {
      println("Messenger got banged")
    }
  }
}

//Actor that adds new pages into the system.
class Registrar extends Actor{
  def receive = {
    case RegisterRequest(name: String, encodedPublicKey: Array[Byte]) => {
      val index = Backend.registerNewUser(name, encodedPublicKey)
      sender ! RegisterResponse(index)
    }
  }
}

class Poster extends Actor{
  def receive = {
    case NewPost(receiverId: Int, fbPost: FbPost) => {
      Backend.pages(receiverId).postsList.posts = Backend.pages(receiverId).postsList.posts :+ fbPost
//      println("Posted on " + Backend.pages(receiverId).profile.name + " postlist.")
//      println(Backend.pages(receiverId).postsList.posts)
    }
  }
}

class Friender extends Actor{
  def receive = {

    case AddPendingFriend(id: Int, session: Array[Byte], friendId: Int, self: Friend) => {
      if(isValidFriend(friendId, self)){
        val pfl = Backend.pages(friendId).pendingFriendsList.pendingFriends :+ self
        Backend.pages(friendId).pendingFriendsList.pendingFriends = pfl
        sender ! "Server : Added pending friend successfully!"
      }
      else{
        sender ! "Cannot add pending friend."
      }
    }

    case AcceptFriends(id: Int, session: Array[Byte], newFriends: List[Friend], selfCards: List[Friend]) => {

      //Add all of the friends that have been deemed accepted
      var fl = Backend.pages(id).friendsList.friends
      fl = fl ::: newFriends
      Backend.pages(id).friendsList.friends = fl

      //Add your friend card to all the friends you just added.
      for (i <- newFriends.indices){
        fl = Backend.pages(newFriends(i).id).friendsList.friends
        fl = fl :+ selfCards(i)
        Backend.pages(newFriends(i).id).friendsList.friends = fl
      }

      //Clear your pending friends list
      Backend.pages(id).pendingFriendsList.pendingFriends = List()

      sender ! "Accepted friends from pending friends."

    }

  }

  //This helper method checks to see when adding yourself to another persons pending friends list,
  //that you are a valid person to add.
  //Things to avoid: Adding self to self, adding self to people who youre already friends with.
  def isValidFriend(friendId: Int, selfFriend: Friend): Boolean = {

    //Check to see if self is trying to add self
    if(friendId == selfFriend.id){
      return false
    }

    //Check to see that the friend already doesnt have you as a real friend.
    val fl = Backend.pages(friendId).friendsList.friends
    if(fl.contains(selfFriend)){
      return false
    }

    //Check to see that self is not already a pending friend for the friend
    val pfl = Backend.pages(friendId).pendingFriendsList.pendingFriends
    if(pfl.contains(selfFriend)){
      return false
    }

    return true

  }

}

class LoginActor extends Actor {

  import Backend._

  val rng = new SecureRandom

  def receive = {

    case LoginRequest(id: Int) => {
      //Create a new challenge
      val challenge = new Array[Byte](32)
      rng.nextBytes(challenge)

      //Remember the challenge in the backend
      challenges(id) = challenge

      //Reply with a challenge request.
      sender ! ChallengeResponse(challenge)
    }

    case SignedChallenge(id: Int, signedChallenge: Array[Byte]) => {

      //Get the public key from the backend.
      val publicKey = rsa.decodePublicKey(pages(id).publicKeyEncoded)

      //POSSIBLY CHECK IF CHALLENGE IS 0.

      //Check to see if the challenge is signed correctly.
      if(rsa.verify(publicKey,signedChallenge,challenges(id))){
        //Create a new random session and store the session
        val session = new Array[Byte](32)
        rng.nextBytes(session)
        sessions(id) = session

        //Set the old challenge to 0 to ensure no duplicate logins. IMPORTANT. CAREFUL
        val challenge = Array.fill[Byte](0)(32)
        challenges(id) = challenge

        //Send the session
        sender ! LoginResponse(session,1)
      }
      else{
        //Did not pass signature. Send back the HAMMER.
        sender ! LoginResponse(Array.empty[Byte],0)
      }

    }

  }


}
