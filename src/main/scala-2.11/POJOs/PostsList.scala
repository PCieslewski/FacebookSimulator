package POJOs

class PostsList(posts_p: List[FbPost]) {

  var posts: List[FbPost] = posts_p

  //Empty constructor
  def this(){
    this(List())
  }

}
