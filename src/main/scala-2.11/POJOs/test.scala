package POJOs

import spray.json._

//import spray.json._

object test{

  def main(args: Array[String]) {
    val myPro: Profile = new Profile("will")
    myPro.birthday = "june"
    myPro.name = "will"
//    myPro.status = "Single"

//    println(myPro)

    val will: Friend = new Friend(0,"Pawel")
    will.id = 3
    will.name = "livesey"

    println(will)
    val test = new Page("Pawel")


    import POJOs.CustomJsonProtocol._

//    val aa = new Text("Hey", "Will").toJson
//    println("Json stuff:")
//    println(aa)
//
//    val bb = aa.convertTo[Text]
//    println("Back to (scala) object")
//    println(bb)

    println("PICTURE!")
    val picIn = new Picture("my pic!").toJson
    println(picIn)

    val picOut = picIn.convertTo[Picture]
    println(picOut.pic)

    val tt = new Profile("William")
    tt.birthday = "June"
    tt.profilePic.pic = "MY PICTURE!!!"
    tt.relationshipStatus = "Single"

    println(tt)
    println("*****")
    val jsonPro = tt.toJson
    println(jsonPro)

    println("DE ______")
    val normPro = jsonPro.convertTo[Profile]
    println(normPro)

    println("Friend Example:")
    val friend: Friend = new Friend(3, "NG")
    val jsonFriend = friend.toJson
    println("JSON:")
    println(jsonFriend)

    println("Back to friend object")
    val normFriend = jsonFriend.convertTo[Friend]
    println(normFriend)


    println("FOOD EX!")
    val nums: List[Int] = List(1,2,3)
    val food: Food = new Food(nums)
    println(food.toJson)

  }
}
