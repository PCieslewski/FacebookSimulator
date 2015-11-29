package Client

import akka.actor._

import scala.collection.mutable.ArrayBuffer

object Main{
  def main(args: Array[String]) {

    implicit val clientSystem = ActorSystem()
    val numClients = 1000

    val keyboardInput = clientSystem.actorOf(Props(new KeyboardInput()), name = "SuperBob")
    var actors: ArrayBuffer[ActorRef] = new ArrayBuffer()

    // the handler actor replies to incoming HttpRequests
    for (i <- 0 until numClients) {

      //      test += "Bob" + i
      val s = clientSystem.actorOf(Props(new Client("Bob" + i, numClients)), name = "Bob" + i) //make an array
      actors += s
    }
    
    var previous: Int = 0
    var isntFirstIteration: Boolean = false

    while (true) {
      println("Type a number to listen to that actor: ")
      val actorNum = scala.io.StdIn.readInt() //actor number
      keyboardInput ! Toggle(actors(actorNum))

      if(isntFirstIteration) {
        keyboardInput ! Toggle(actors(previous))
      }

      previous = actorNum
      isntFirstIteration = true
    }

  }
}