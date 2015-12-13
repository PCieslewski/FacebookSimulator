package POJOs

case class FbMessage(message: String,
                     id: Int,
                     session: Array[Byte]){}
//                     publicKeyEncoded: Array[Byte],
//                     aesKeyEncrypted: Array[Byte]) {}
