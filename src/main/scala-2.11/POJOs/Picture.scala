package POJOs

class Picture {
  var picture: Array[Byte] = Array.fill[Byte](1)(0)

  override def toString() : String = {
    var myString = "Picture Array: \n"
    var index = 0
    for(text <- picture) {
      myString += "Index #" + index + "\n"
      index += 1
      myString += (text + "\n")
    }
    return myString
  }
}
