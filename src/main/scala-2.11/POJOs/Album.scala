package POJOs

class Album(pictures_p: List[Picture]) {

  var pictures = pictures_p

  //Empty Constructor
  def this(){
    this(List())
  }

}
