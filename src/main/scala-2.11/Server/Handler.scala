package Server

import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import akka.actor._
import spray.can.Http
import spray.can.server.Stats
import spray.util._
import spray.http._
import HttpMethods._
import MediaTypes._
import spray.can.Http.RegisterChunkHandler

class Handler extends Actor with ActorLogging {
  implicit val timeout: Timeout = 1.second // for the actor 'asks'
  import context.dispatcher // ExecutionContext for the futures and scheduler

  def receive = {
    // when a new connection comes in we register ourselves as the connection handler
    case _: Http.Connected => sender ! Http.Register(self)

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
      sender ! HttpResponse(entity = "PONG!")

    case HttpRequest(GET, Uri.Path("/hello"), headers: List[HttpHeader], entity: HttpEntity, _) =>
      val name = entity.data.asString
      println(getValue(headers,"client-id"))
      sender ! HttpResponse(entity = "Hello there " + name + "!")

  }

  def getValue(headers: List[HttpHeader], name: String): String ={
    val itr = headers.iterator;
    while(itr.hasNext){
      val header = itr.next();
      if(header.name.equals(name)){
        return header.value
      }
    }
    return null
  }

}
