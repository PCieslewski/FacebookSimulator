package Client

import java.util

import POJOs._
import akka.actor.{ActorSystem, Actor}
import akka.pattern.ask
import akka.io.IO
import akka.util.Timeout
import spray.can.Http
import spray.httpx.unmarshalling._
import spray.httpx._
import spray.http.HttpMethods._
import spray.http.{HttpHeader, Uri, HttpRequest, HttpResponse}
import spray.json.JsObject

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import scala.concurrent.duration._

import spray.client.pipelining._
import spray.httpx.SprayJsonSupport._




class Client(name_p: String) extends Actor {

  import context.dispatcher
  implicit var ActorSystem = context.system
  implicit val timeout: Timeout = 15.second // for the actor 'asks'
  import CustomJsonProtocol._

  //Each client knows their name.
  var id: Int = 0
  val name: String = name_p
  val baseURI = "http://localhost:8080/"

  def getTestMsg() = {
    val pipeline: HttpRequest => Future[TestMsg] = sendReceive ~> unmarshal[TestMsg]
    val f: Future[TestMsg] = pipeline(Get("http://localhost:8080/hello"))
    f onComplete {
      case Success(r) => {
        println("I AM A GOD!")
        println(r.a)
        println(r.b)
      }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }

  }

  def registerSelf() = {
    val pipeline: HttpRequest => Future[RegisterResponse] = sendReceive ~> unmarshal[RegisterResponse]
    val f: Future[RegisterResponse] = pipeline(Post("http://localhost:8080/register", new RegisterRequest(name)))
    f onComplete {
      case Success(r) => {
        println("Registered. Got ID: " + r.id)
      }
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }

  registerSelf()

  val response: Future[HttpResponse] = (IO(Http) ? HttpRequest(GET, Uri("http://localhost:8080/hello"))).mapTo[HttpResponse]
  response onComplete {
    case Success(r) => {
      r.entity.asString
    }
    case Failure(t) => println("An error has occured: " + t.getMessage)
  }

  //Testing a function call!
//  val fPong = getPong
//  fPong onSuccess {
//    case (str: String) => println("YES! " + str)
//  }

  //Testing just a ping -- should print pong back.
//  val response: Future[HttpResponse] = (IO(Http) ? HttpRequest(GET, Uri("http://localhost:8080/ping"))).mapTo[HttpResponse]
//  response onComplete {
//    case Success(post) => println(post.entity.asString)
//    case Failure(t) => println("An error has occured: " + t.getMessage)
//  }

  //Testing passing something through a request. Passes Pawel and returns Hello there Pawel.
  //var headers = List(new RawHeader("client-name",name),new RawHeader("client-id",id))
  //headers.add(new RawHeader("client-name",name))
  //headers.add(new RawHeader("client-id",id))


//  val response2: Future[HttpResponse] = (IO(Http) ? HttpRequest(GET, Uri("http://localhost:8080/hello"), headers, entity = "Pawel")).mapTo[HttpResponse]
//  response2 onComplete {
//    case Success(post) => println(post.entity.asString)
//    case Failure(t) => println("An error has occured: " + t.getMessage)
//  }

//  val response3: Future[HttpResponse] = (IO(Http) ? HttpRequest(GET, Uri("http://localhost:8080/testObject"))).mapTo[HttpResponse]
//  response3 onComplete {
//    case Success(post) => {
//      val respOb =
//    }
//    case Failure(t) => println("An error has occured: " + t.getMessage)
//  }


  def receive = {
    case _ => {
      println("Msg.")
    }
  }

  def getPong: Future[String] = {
    val fResponse: Future[HttpResponse] = (IO(Http) ? HttpRequest(GET, Uri("http://localhost:8080/ping"))).mapTo[HttpResponse]
    val fString: Future[String] = fResponse.flatMap{
      case (resp: HttpResponse) => Future{resp.entity.asString}
    }
    return fString
  }



}
