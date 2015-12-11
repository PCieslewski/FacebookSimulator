package POJOs

import scala.collection.mutable

object test{

  def main(args: Array[String]) {
//    val myPro: Profile = new Profile("will")
//    myPro.birthday = "june"
//    myPro.name = "will"
//    myPro.status = "Single"
//
//    println(myPro)
//
//    val will: Friend = new Friend(0,"Will")
//
//    println(will)
//    val test = new Page("Pawel")
//
//    println(test.profile)

    val l = mutable.ArrayBuffer[Int]()

    l += 0

    l.insert(1,69)

    println(l(1))

  }
}
