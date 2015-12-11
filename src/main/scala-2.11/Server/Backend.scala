package Server

import java.security.SecureRandom

import POJOs._
import POJOs.crypto.rsa
import akka.actor._

import scala.collection.mutable

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

  def registerNewUser(name: String, encodedPublicKey: Array[Byte]): Int = {
    pages += new Page(name, encodedPublicKey)
    sessions += Array.empty[Byte]
    challenges += Array.empty[Byte]

    index = index + 1
    return index - 1
  }

  def getIdFromName(name: String): Int = {
    for(i <- pages.indices){
      if(pages(i).profile.name.equals(name)){
        return i
      }
    }
    return -1
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

//Actor who friends 2 people together and updats the data structures.
class Friender extends Actor{
  def receive = {
    case AddFriend(requesterId: Int, requesterName: String, friendName: String) => {
      val requester: Friend = Friend(requesterId, requesterName)
      val friend: Friend = Friend(Backend.getIdFromName(friendName),friendName)
      if(requester.id < 0 || friend.id < 0){
        sender ! "Friend not found."
      }
      else {
        if(requester == friend){
          sender ! "Cannot add self as a friend."
        }
        else {
          addFriend(requester, friend)
          addFriend(friend, requester)
          sender ! "Friend added successfully."
        }
      }
    }
  }

  def addFriend(f1: Friend, f2: Friend){
//    println(f1.name + " adding " + f2.name)
    var fList = Backend.pages(f1.id).friendsList.friends
    if(!fList.contains(f2)){
      fList = fList :+ f2
      Backend.pages(f1.id).friendsList.friends = fList
//      println(f1)
//      println(Backend.pages(f1.id).friendsList.friends)
    }
    else{
//      println("friend already added. "+ f1.name + " already added " + f2.name)
    }
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
