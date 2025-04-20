package com.passmanager.utils

import org.mindrot.jbcrypt.BCrypt
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.spec.KeySpec
import java.util.Base64

object SecurityUtils {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/ECB/PKCS5Padding"
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    
    fun hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())
    
    fun verifyPassword(password: String, hash: String): Boolean = BCrypt.checkpw(password, hash)
    
    fun encryptPassword(password: String, masterKey: String): String {
        val salt = "pass-manager-salt".toByteArray() // In production, use a unique salt per user
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val spec: KeySpec = PBEKeySpec(masterKey.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        val key = SecretKeySpec(tmp.encoded, ALGORITHM)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes = cipher.doFinal(password.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }
    
    fun decryptPassword(encryptedPassword: String, masterKey: String): String {
        val salt = "pass-manager-salt".toByteArray() // In production, use a unique salt per user
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val spec: KeySpec = PBEKeySpec(masterKey.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        val key = SecretKeySpec(tmp.encoded, ALGORITHM)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword))
        return String(decryptedBytes)
    }
} 