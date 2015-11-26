package Client

import akka.actor._

object Main{
  def main(args: Array[String]) {

    implicit val clientSystem = ActorSystem()

    // the handler actor replies to incoming HttpRequests
    for(i <- 1 to 1) {
      clientSystem.actorOf(Props(new Client("Bob"+i)), name = "Bob"+i)
    }

  }
}