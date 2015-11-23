package POJOs

class Post {
  var postId: Int = _
  var postText: Text = _
  var postPicture: Picture = _
  var comments: List[Text] = _

  override def toString() : String = {
    var myString = "Post Id: " + postId + "\nPost's message: " + postText
    myString += "\n Comments: "
    var commentNumber = 0
    for(text <- comments) {
      myString += "Comment Number #" + commentNumber + "\n"
      commentNumber += 1
      myString += (text + "\n")
    }
    return myString
  }
}
