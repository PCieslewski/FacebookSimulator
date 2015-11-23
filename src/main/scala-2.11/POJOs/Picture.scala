package POJOs

class Picture {
  var pictureArray: Array[Byte] = _

  override def toString() : String = {
    var myString = "Picture Array: \n"
    var index = 0
    for(text <- pictureArray) {
      myString += "Index #" + index + "\n"
      index += 1
      myString += (text + "\n")
    }
    return myString
  }
}
