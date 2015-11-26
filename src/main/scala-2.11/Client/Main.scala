package Client

import akka.actor._

object Main{
  def main(args: Array[String]) {

    implicit val clientSystem = ActorSystem()

    // the handler actor replies to incoming HttpRequests
    for(i <- 0 to 5) {
      clientSystem.actorOf(Props(new Client("Bob"+i,5)), name = "Bob"+i)
    }

  }
}