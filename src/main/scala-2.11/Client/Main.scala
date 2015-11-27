package Client

import akka.actor._

object Main{
  def main(args: Array[String]) {

    implicit val clientSystem = ActorSystem()
    val numClients = 5

    // the handler actor replies to incoming HttpRequests
    for(i <- 0 until numClients) {
      clientSystem.actorOf(Props(new Client("Bob"+i,numClients)), name = "Bob"+i)
    }

  }
}