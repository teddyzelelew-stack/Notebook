package com.example.util

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptographyHelper {

    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY_ALGORITHM = "AES"
    private const val DIGEST_ALGORITHM = "SHA-256"

    // Simple 16-byte fixed IV for robust single-field storage demo
    private val FIXED_IV = byteArrayOf(
        0x12, 0x34, 0x56, 0x78, 0x90.toByte(), 0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte(),
        0x12, 0x34, 0x56, 0x78, 0x90.toByte(), 0xAB.toByte(), 0xCD.toByte(), 0xEF.toByte()
    )

    /**
     * Encrypt a string using AES and a derived key from the user's password
     */
    fun encrypt(plainText: String, password: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            val keyBytes = MessageDigest.getInstance(DIGEST_ALGORITHM).digest(password.toByteArray(Charsets.UTF_8))
            val secretKeySpec = SecretKeySpec(keyBytes, KEY_ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            val ivSpec = IvParameterSpec(FIXED_IV)
            
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Decrypt a string using AES and a derived key from the user's password
     */
    fun decrypt(encryptedText: String, password: String): String {
        if (encryptedText.isEmpty()) return ""
        return try {
            val keyBytes = MessageDigest.getInstance(DIGEST_ALGORITHM).digest(password.toByteArray(Charsets.UTF_8))
            val secretKeySpec = SecretKeySpec(keyBytes, KEY_ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            val ivSpec = IvParameterSpec(FIXED_IV)
            
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec)
            val decodedBytes = Base64.decode(encryptedText, Base64.NO_WRAP)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            "[የተሳሳተ የይለፍ ቃል - መፍታት አልተቻለም]" // "Incorrect Password - Decryption Failed" in Amharic
        }
    }
}
