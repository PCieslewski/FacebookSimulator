package Examples

import java.security._
import POJOs.crypto._

object RSAExample {

  def main(args: Array[String]) {

    val aeskey = aes.generateSecretKey
    val test: String = "This is a test."

    //Testing AES
    println(test)
    val testEncrypted = aes.encrypt(aeskey,test.getBytes())
    println(new String(testEncrypted))
    val testDecrypted = aes.decrypt(aeskey,testEncrypted)
    println(new String(testDecrypted))
    println("--------------------\n")

    //Testing Secure RNG
    val rng = new SecureRandom
    println(rng.nextInt())
    println("--------------------\n")

    //Testing RSA
    println("RSA HERE!")
    val kp = rsa.generateKeyPair()
    val pubKey = kp.getPublic
    val privKey = kp.getPrivate

    println(test)
    val testEncryptedRSA = rsa.encrypt(pubKey,test.getBytes())
    println(new String(testEncryptedRSA))
    val testDecryptedRSA = rsa.decrypt(privKey,testEncryptedRSA)
    println(new String(testDecryptedRSA))
    println("--------------------\n")

    //Testing RSA Signature
    val name = "Bob453"
    val sig = rsa.sign(privKey,name.getBytes())
    println(rsa.verify(pubKey,sig,name.getBytes()))
    println("--------------------\n")

    //Test encrypting key, then decrypting and using it.
    val encryptedAESKey = rsa.encrypt(pubKey, aeskey.getEncoded)
    val decryptedAESKey = aes.decodeSecretKey(rsa.decrypt(privKey, encryptedAESKey))
    println(new String(aes.decrypt(decryptedAESKey, testEncrypted)))

  }

}
