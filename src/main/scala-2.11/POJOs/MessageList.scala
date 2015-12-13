package POJOs

class MessageList(messages_p: List[FbMessage]) {

  var messages: List[FbMessage] = messages_p

  //Empty constructor
  def this(){
    this(List())
  }

}
