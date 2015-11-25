package POJOs

object test{

  def main(args: Array[String]) {
    val myPro: Profile = new Profile("will")
    myPro.birthday = "june"
    myPro.name = "will"
    myPro.status = "Single"

    println(myPro)

    val will: Friend = new Friend(0,"Pawel")
    will.id = 3
    will.name = "livesey"

    println(will)
    val test = new Page("Pawel")

    println(test.profile)

  }
}
