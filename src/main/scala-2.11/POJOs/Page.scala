package POJOs

class Page {
  var profile: Profile = _
  var posts: List[Post] = _
  var pictures: Album = _

  override def toString() : String = {
    var myString = "Profile: \n" + profile + "\n"
    myString += "\n List of posts: "
    var postNumber = 0
    for(text <- posts) {
      myString += "Comment Number #" + postNumber + "\n"
      postNumber += 1
      myString += (text + "\n")
    }
    return myString
  }
}
