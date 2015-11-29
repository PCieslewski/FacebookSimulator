package WillTest

import akka.actor._

import scala.collection.mutable.ArrayBuffer

sealed trait Msg
case class printYoShit(ref: ActorRef) extends Msg
case class Stop() extends Msg
case class SetOne() extends Msg

class FacebookStuff(step: Int, id: Int) extends Actor {
  var count: Int = 0

  var work: Int = 1

  var isRunning : Boolean = false;

  def receive = {
    case "here" => {
      Thread sleep(100)
      count += step
      self ! "here"
    }

    case SetOne() => {
      if(isRunning) {
        context stop self
      }
      else {
        isRunning = true
        self ! printYoShit(sender)
      }
    }


    case Stop() => {
//      println("WORK BEFORE: " + work)
      work = 0

      //trying this:
      sender ! "here"
      context stop self

//      println("WORK AFTER: " + work)
//      println("frozen")
    }

    case printYoShit(ref: ActorRef) => {
//      println("counter is at: " + count)

//      while (work == 1) {
//        Thread sleep(1000)
//      if(work != 0) {
//        while(true) {
          Thread sleep (1000)
          println("Counter #" + id + " is at: " + count)
//        }
//        println("Value of work: " + work)
//      }
//      else {
//        println("gotcha 0")
//      }
//        self ! Stop()
//      }
//println("LOLOLO")
//      context.actorSelection("akka://WillsWorld/user/facebookStuff") ! "Stop"
//      context.actorSelection("akka://WillsWorld/user/facebookStuff2") ! "Stop"
//      ref ! "here"
      self ! printYoShit(self)
    }


  }
}

class KeyBoardChecker extends Actor {
//  var printThese = new ArrayBuffer[Int]()

  def receive = {
    case "here" => {
      val x = scala.io.StdIn.readInt()

//      if(x == -1) {
//        printThese = new ArrayBuffer[Int]()
//        println(printThese.toString())
//      }
//      else {
//        printThese += x
//        println("ELSE " + printThese.toString())
//      }
//
//      self ! "here"

//      println("Got here??")

//      if(x == 3) {
//        context.actorSelection("akka://WillsWorld/user/facebookStuff") ! Stop()
////        context.actorSelection("akka://WillsWorld/user/facebookStuff2") ! Stop()
//      }

      if(x == 1) {
        context.actorSelection("akka://WillsWorld/user/facebookStuff") ! SetOne()
      }
      else if(x == 2) {
        context.actorSelection("akka://WillsWorld/user/facebookStuff2") ! SetOne()
      }
      else {
        self ! "here"
      }

      self ! "here"
    }
  }
}

object Main extends App {
  val system = ActorSystem("WillsWorld")

  val facebookStuff = system.actorOf(Props(new FacebookStuff(1, 1)), name = "facebookStuff")
  val facebookStuff2 = system.actorOf(Props(new FacebookStuff(10, 2)), name = "facebookStuff2")
  val keyboardChecker = system.actorOf(Props(new KeyBoardChecker), name= "keyboardChecker")

//  println("P1: " + facebookStuff.path)
//  println("P2: " + facebookStuff2.path)

  println("Enter ONLY numbers 1 or 2 to view counters")

  //Start counters
  facebookStuff ! "here"
  facebookStuff2 ! "here"

  keyboardChecker ! "here"

}