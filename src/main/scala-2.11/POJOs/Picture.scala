package POJOs

class Picture(picture_p: String) {
//  var picture: Array[Byte] = Array.fill[Byte](1)(0)
//
//  override def toString() : String = {
//    var myString = "Picture Array: \n"
//    var index = 0
//    for(text <- picture) {
//      myString += "Index #" + index + "\n"
//      index += 1
//      myString += (text + "\n")
//    }
//    return myString
//  }
  var picture: String = picture_p

  def this() {
    this("")
  }

  override def toString(): String = {
    val myString = "Your 'picture': " + picture
    return myString
  }
}
