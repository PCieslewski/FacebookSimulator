package Client

import akka.actor.{ActorRef, Actor}

case class Toggle(ref: ActorRef)

class KeyboardInput extends Actor {
  def receive = {
    case Toggle(ref: ActorRef)  =>
      ref ! "Toggle"
  }
}
