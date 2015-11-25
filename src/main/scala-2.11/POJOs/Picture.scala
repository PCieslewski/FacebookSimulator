package POJOs

//class Picture(bytes: Array[Byte]) {
//  var pictureRaw: Array[Byte] = bytes
//
//  //Empty constructor
//  def this() {
//    this(Array.fill[Byte](1)(0))
//  }
//}

class Picture(pic_p: String) {

  var pic: String = pic_p

  def this() {
    this("")
  }

  override def toString(): String = {
    val myString = "Pic string: " + pic;
    return myString
  }

}