package POJOs

class Picture(bytes: Array[Byte]) {
  var pictureRaw: Array[Byte] = bytes

  //Empty constructor
  def this() {
    this(Array.fill[Byte](1)(0))
  }
}
