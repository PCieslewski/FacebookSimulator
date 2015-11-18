package Client

import akka.actor.{ActorSystem, Actor}
import akka.pattern.ask
import akka.io.IO
import akka.util.Timeout
import spray.can.Http
import spray.http.HttpMethods._
import spray.http.{Uri, HttpRequest, HttpResponse}

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.duration._




class Client(name_p: String) extends Actor {

  import context.dispatcher
  implicit var ActorSystem = context.system
  implicit val timeout: Timeout = 15.second // for the actor 'asks'

  //Each client knows their name. It is their identifier.
  val name: String = name_p

  //Testing just a ping -- should print pong back.
  val response: Future[HttpResponse] = (IO(Http) ? HttpRequest(GET, Uri("http://localhost:8080/ping"))).mapTo[HttpResponse]
  response onComplete {
    case Success(post) => println(post.entity.asString)
    case Failure(t) => println("An error has occured: " + t.getMessage)
  }


  def receive = {
    case _ => {
      println("Msg.")
    }
  }

}
