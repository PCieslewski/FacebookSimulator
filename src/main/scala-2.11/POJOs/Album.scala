package POJOs

class Album() {

  val pictures: List[Picture] = List()

  override def toString() : String = {
    var myString = "Album Array: \n"
    var index = 0
    for(pics <- pictures) {
      myString += "Picture Number #" + index + "\n"
      index += 1
      myString += (pics + "\n")
    }
    return myString
  }
}
