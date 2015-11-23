package POJOs

class Page(name: String) {
  var profile: Profile = new Profile(name)
  var posts: List[Post] = List()
  var pictures: Album = new Album()

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
