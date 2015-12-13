package POJOs

class Page(name_p: String, publicKeyEncoded_p: Array[Byte]) {
  var name = name_p
  var publicKeyEncoded = publicKeyEncoded_p
  var profileEncrypted: Array[Byte] = Array.empty[Byte]
  var postsList: PostsList = new PostsList()
  var album: Album = new Album()
  var friendsList: FriendsList = new FriendsList()
  var pendingFriendsList: PendingFriendsList = new PendingFriendsList()
}

