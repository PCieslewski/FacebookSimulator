package POJOs

class Post(postId_p: Int, postText_p: Text, postPicture_p: Picture) {
  var postId: Int = postId_p
  var postText: Text = postText_p
  var postPicture: Picture = postPicture_p
  var comments: List[Text] = List()

  def this(postId_p: Int, postText_p: Text) {
    this(postId_p, postText_p, null)
  }

  def this(postId_p: Int, postPicture_p: Picture) {
    this(postId_p, null, postPicture_p)
  }

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
