package POJOs

import spray.json

import scala.collection.mutable
import CustomJsonProtocol._
import spray.json._

object test{

  def main(args: Array[String]) {

    val pro = Profile("Pawel","11/12/1992",Picture(Array.empty[Byte]),"Taken")
    val j = pro.toJson

    println(j.prettyPrint)

    val pro2 = j.convertTo[Profile]

    println(pro2.name)


  }
}
