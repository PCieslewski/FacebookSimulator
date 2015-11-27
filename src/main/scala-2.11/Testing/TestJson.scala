package Testing

import Client.RNG
import POJOs._
import spray.json
import spray.json._

object TestJson{

  def main(args: Array[String]) {

    println(RNG.getRandNum(2))

  }
}
