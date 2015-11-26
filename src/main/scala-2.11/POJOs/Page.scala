package POJOs

class Page(name: String) {
  var profile: Profile = new Profile(name)
  var postsList: PostsList = new PostsList()
  var album: Album = new Album()
  var friendsList: FriendsList = new FriendsList()
}
