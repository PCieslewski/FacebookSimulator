package POJOs

sealed class Message
case class TestMsg(a: Int, b: String) extends Message

//Messages used to register a client to the server.
case class RegisterRequest(name: String) extends Message
case class RegisterResponse(id: Int) extends Message
