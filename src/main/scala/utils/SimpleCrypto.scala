package com.androidsnippets
//http://www.androidsnippets.com/encryptdecrypt-strings

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object SimpleCrypto {
  def encrypt(seed: String, cleartext: String): String = {
    var rawKey = getRawKey(seed.getBytes())
    var result = encrypt(rawKey, cleartext.getBytes())
    return toHex(result)
  }

  def decrypt(seed: String, encrypted: String): String = {
    var rawKey = getRawKey(seed.getBytes())
    var enc = toByte(encrypted)
    var result = decrypt(rawKey, enc)
    return new String(result)
  }

  private def getRawKey(seed: Array[Byte]): Array[Byte] ={
    val kgen = KeyGenerator.getInstance("AES")
    val sr = SecureRandom.getInstance("SHA1PRNG")
    sr.setSeed(seed)
    kgen.init(128, sr)
    val skey = kgen.generateKey
    return skey.getEncoded
  }

  private def encrypt(raw: Array[Byte], clear: Array[Byte]): Array[Byte] ={
    val skeySpec = new SecretKeySpec(raw, "AES")
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
    val encrypted = cipher.doFinal(clear)
    return encrypted
  }

  private def decrypt(raw: Array[Byte], encrypted: Array[Byte]): Array[Byte] ={
    val skeySpec = new SecretKeySpec(raw, "AES")
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.DECRYPT_MODE, skeySpec)
    val decrypted = cipher.doFinal(encrypted)
    return decrypted
  }

  def toHex(txt: String): String ={
    toHex(txt.getBytes())
  }
  
  def fromHex(hex: String): String ={
    toByte(hex).toString
  }

  def toByte(hexString: String): Array[Byte] ={
    val len = hexString.length/2
    val result:Array[Byte] = new Array[Byte](len)
    for(i <- (0 to len-1)){
      result(i) = java.lang.Integer.valueOf(hexString.toString.substring(2*i, 2*i+2), 16).byteValue
    }
    return result
  }

  def toHex(buf: Array[Byte]): String ={
    if (buf == null)
      return ""
    val result = new StringBuffer(2*buf.length)
    for(i <- (0 to buf.length-1))
      appendHex(result, buf(i))

    return result.toString
  }

  val HEX = "0123456789ABCDEF"
  private def appendHex(sb: StringBuffer, b: Byte) ={
    sb.append(HEX.charAt((b>>4)&0x0f)).append(HEX.charAt(b&0x0f))
  }
}

//set shiftwidth=2 tabstop=2 expandtab
