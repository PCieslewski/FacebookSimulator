package Examples

import java.io._
import java.security._
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto._
import javax.crypto.spec.SecretKeySpec
import Examples.crypto.{rsa, aes}

import scala.collection.immutable.Stream
import scala.Function.const

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
    val kp = rsa.generateKeyPair()
    val pubKey = kp.getPublic
    val privKey = kp.getPrivate

    println(test)
    val testEncryptedRSA = rsa.encrypt(pubKey,test.getBytes())
    println(new String(testEncryptedRSA))
    val testDecryptedRSA = rsa.decrypt(privKey,testEncryptedRSA)
    println(new String(testDecryptedRSA))

  }

}

object crypto {

  object rsa {

    def encrypt(key: PublicKey, data: Array[Byte]): Array[Byte] = {
      val cipher = Cipher.getInstance("RSA")
      cipher.init(Cipher.ENCRYPT_MODE, key)
      cipher.doFinal(data)
    }

    def decrypt(key: PrivateKey, data: Array[Byte]): Array[Byte] = {
      val cipher = Cipher.getInstance("RSA")
      cipher.init(Cipher.DECRYPT_MODE, key)
      cipher.doFinal(data)
    }

    def sign(key: PrivateKey, data: Array[Byte]): Array[Byte] = {
      val signer = Signature.getInstance("SHA1withRSA")
      signer.initSign(key)
      signer.update(data)
      signer.sign
    }

    def verify(key: PublicKey, signature: Array[Byte], data: Array[Byte]): Boolean = {
      val verifier = Signature.getInstance("SHA1withRSA")
      verifier.initVerify(key)
      verifier.update(data)
      verifier.verify(signature)
    }

    def generateKeyPair(): KeyPair = {
      val keyGen = KeyPairGenerator.getInstance("RSA")
      keyGen.initialize(512, new SecureRandom())
      keyGen.generateKeyPair()
    }

  }

  object aes {
    def encrypt(key: SecretKey, data: Array[Byte]): Array[Byte] = {
      val cipher = Cipher.getInstance("AES")
      cipher.init(Cipher.ENCRYPT_MODE, key)
      cipher.doFinal(data)
    }

    def decrypt(key: SecretKey, data: Array[Byte]): Array[Byte] = {
      val cipher = Cipher.getInstance("AES")
      cipher.init(Cipher.DECRYPT_MODE, key)
      cipher.doFinal(data)
    }

    def generateSecretKey: SecretKey = {
      val generator = KeyGenerator.getInstance("AES")
      generator.init(128)
      generator.generateKey
    }

    def decodeSecretKey(encodedKey: Array[Byte]): SecretKey =
      new SecretKeySpec(encodedKey, "AES")
  }

}
