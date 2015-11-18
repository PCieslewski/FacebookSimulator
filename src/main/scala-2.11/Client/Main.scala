package Client

import akka.actor._

object Main{
  def main(args: Array[String]) {

    implicit val clientSystem = ActorSystem()

    // the handler actor replies to incoming HttpRequests
    val handler = clientSystem.actorOf(Props(new Client("Pawel")), name = "Pawel")

  }
}