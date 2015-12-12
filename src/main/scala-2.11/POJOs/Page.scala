package POJOs

class Page(name: String, publicKeyEncoded_p: Array[Byte]) {
  var publicKeyEncoded = publicKeyEncoded_p
  var profile: Profile = new Profile(name, "", new Picture(Array.fill[Byte](0)(1)), "Single")
  var postsList: PostsList = new PostsList()
  var album: Album = new Album()
  var friendsList: FriendsList = new FriendsList()
  var pendingFriendsList: PendingFriendsList = new PendingFriendsList()
}

