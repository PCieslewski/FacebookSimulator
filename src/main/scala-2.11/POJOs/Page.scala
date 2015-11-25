package POJOs

class Page(name: String) {
  var profile: Profile = new Profile(name)
  var postsList: PostsList = new PostsList()
  var pictures: Album = new Album()
  var friendsList: FriendsList = new FriendsList(null) //CHANGE FROM NULL
}
