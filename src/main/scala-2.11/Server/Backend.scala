package Server

import POJOs.{RegisterResponse, RegisterRequest, Page}
import akka.actor._

import scala.collection.mutable

object Backend {

  implicit val backendSystem = ActorSystem()

  var pages = new mutable.MutableList[Page]
  var index = 0
  val registrar = backendSystem.actorOf(Props(new Registrar()), name = "Registrar")

  def registerNewUser(name: String): Int = {
    pages += new Page(name)
    index = index + 1
    return index - 1
  }

}

class Registrar extends Actor{

  def receive = {
    case RegisterRequest(name: String) => {
      val index = Backend.registerNewUser(name)
      sender ! RegisterResponse(index)
    }
  }

}
