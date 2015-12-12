package POJOs

case class Friend(id: Int,
                  name: String,
                  publicKeyEncoded: Array[Byte],
                  aesKeyEncrypted: Array[Byte]) {}
