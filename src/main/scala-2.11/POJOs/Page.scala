package POJOs

class Page(name: String) {
  var profile: Profile = new Profile(name, "", new Picture(Array.fill[Byte](0)(1)), "Single")
  var postsList: PostsList = new PostsList()
  var album: Album = new Album()
  var friendsList: FriendsList = new FriendsList()
}
