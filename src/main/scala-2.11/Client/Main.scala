package Client

import akka.actor._

import scala.collection.mutable.ArrayBuffer

object Main{
  def main(args: Array[String]) {

    implicit val clientSystem = ActorSystem()
    var numClients = 1000

    if(!args.isEmpty){
      numClients = args(0).toInt
    }

    val keyboardInput = clientSystem.actorOf(Props(new KeyboardInput()), name = "SuperBob")
    var actors: ArrayBuffer[ActorRef] = new ArrayBuffer()

    // the handler actor replies to incoming HttpRequests
    for (i <- 0 until numClients) {
      val s = clientSystem.actorOf(Props(new Client("Bob" + i, numClients)), name = "Bob" + i) //make an array
      actors += s
    }

    println("Number of clients simulated: " + numClients)

    var previous: Int = 0
    var isntFirstIteration: Boolean = false

//    var actorNum = 0

    while (true) {
      try {
        println("Type a number to listen to that actor: ")

        val actorNum = scala.io.StdIn.readInt() //actor number

//        val test = new java.util.Scanner(System.in)
//        actorNum = test.nextInt()

//      val actorNum2 = scala.io.Source.stdin.getLines()
//      println(actorNum2.getClass)


//        println("Your choice " + actorNum)
        keyboardInput ! Toggle(actors(actorNum))

        if (isntFirstIteration) {
          keyboardInput ! Toggle(actors(previous))
        }

        previous = actorNum
        isntFirstIteration = true
      }

      catch {
        case poop: Exception =>
      }
    }
  }
}