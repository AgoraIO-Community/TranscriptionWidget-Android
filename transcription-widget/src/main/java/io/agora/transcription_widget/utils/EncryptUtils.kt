package io.agora.transcription_widget.utils

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.text.TextUtils
import io.agora.transcription_widget.internal.constants.Constants
import io.agora.transcription_widget.internal.utils.LogUtils
import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.KeyPair
import java.security.KeyStore
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SignatureException
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.min

object EncryptUtils {
    private const val KEY_ALIAS = "${Constants.TAG}KeyAlias"
    private var mKeyPair: KeyPair? = null
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val PROVIDER = "AndroidKeyStore"
    private val DEFAULT_IV = ByteArray(12)

    /**
     * 加密数字签名（基于HMACSHA1算法）
     *
     * @param encryptText
     * @param encryptKey
     * @return
     * @throws SignatureException
     */
    @JvmStatic
    @Throws(Exception::class)
    fun HmacSHA1Encrypt(encryptText: String, encryptKey: String): String? {
        val rawHmac = try {
            val data = encryptKey.toByteArray(charset("UTF-8"))
            val secretKey = SecretKeySpec(data, "HmacSHA1")
            val mac = Mac.getInstance("HmacSHA1")
            mac.init(secretKey)
            val text = encryptText.toByteArray(charset("UTF-8"))
            mac.doFinal(text)
        } catch (e: InvalidKeyException) {
            throw SignatureException("InvalidKeyException:" + e.message)
        } catch (e: NoSuchAlgorithmException) {
            throw SignatureException("NoSuchAlgorithmException:" + e.message)
        } catch (e: UnsupportedEncodingException) {
            throw SignatureException("UnsupportedEncodingException:" + e.message)
        }
        return Base64Utils.encode(rawHmac)
    }

    @JvmStatic
    fun MD5(pstr: String): String {
        val md5String = charArrayOf(
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'a',
            'b',
            'c',
            'd',
            'e',
            'f'
        )
        return try {
            val btInput = pstr.toByteArray()
            val mdInst = MessageDigest.getInstance("MD5")
            mdInst.update(btInput)
            val md = mdInst.digest()
            val j = md.size
            val str = CharArray(j * 2)
            var k = 0
            for (i in 0 until j) { // i = 0
                val byte0 = md[i] // 95
                str[k++] = md5String[byte0.toInt() ushr 4 and 0xf] // 5
                str[k++] = md5String[byte0.toInt() and 0xf] // F
            }
            String(str)
        } catch (e: Exception) {
            ""
        }
    }

    @JvmStatic
    fun encryptBySHA256(input: String): String? {
        try {
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(input.toByteArray(StandardCharsets.UTF_8))
            val hexString = StringBuilder()
            for (b in hash) {
                val hex = Integer.toHexString(0xff and b.toInt())
                if (hex.length == 1) {
                    hexString.append('0')
                }
                hexString.append(hex)
            }
            return hexString.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @JvmStatic
    fun encryptByHA256(input: String, targetLength: Int): String? {
        try {
            val md = MessageDigest.getInstance("SHA-256")
            val hashBytes = md.digest(input.toByteArray(StandardCharsets.UTF_8))
            val hexString = StringBuilder()
            for (i in 0 until min(targetLength.toDouble(), hashBytes.size.toDouble())
                .toInt()) {
                val hex = Integer.toHexString(0xff and hashBytes[i].toInt())
                if (hex.length == 1) {
                    hexString.append('0')
                }
                hexString.append(hex)
            }
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return null
    }

    private val secretKey: SecretKey?
        get() {
            try {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val keyStore = KeyStore.getInstance(PROVIDER)
                    keyStore.load(null)
                    val key = keyStore.getKey(KEY_ALIAS, null)
                    if (key == null) {
                        // 生成密钥（会自动保存在keyStore中）
                        val keyGenerator = KeyGenerator.getInstance(
                            KeyProperties.KEY_ALGORITHM_AES, PROVIDER
                        )
                        keyGenerator.init(
                            KeyGenParameterSpec.Builder(
                                KEY_ALIAS,
                                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                            )
                                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                                .build()
                        )
                        // 生成key
                        keyGenerator.generateKey()
                    } else {
                        key as SecretKey
                    }
                } else {
                    // 低版本使用设备唯一标识符加密
                    // 获取Build.FINGERPRINT的字节数组
                    val fingerprintBytes = Build.FINGERPRINT.toByteArray()
                    // 切割字节数组获取前16个字节
                    val keyBytes = Arrays.copyOfRange(fingerprintBytes, 0, 16)
                    // 创建SecretKeySpec对象
                    SecretKeySpec(keyBytes, ALGORITHM)
                }
            } catch (e: Exception) {
                LogUtils.d("getSecretKey error:" + e.message)
            }
            return null
        }

    @JvmStatic
    fun encryptByAes(plainText: String): String? {
        if (TextUtils.isEmpty(plainText)) {
            return ""
        }
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = secretKey
            return if (null != secretKey) {
                //设置解密模式
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                val iv = cipher.iv
                val cipherText = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

                //将认证标签和密文组合保存
                val buffer = ByteBuffer.allocate(Integer.SIZE / 8 + iv.size + cipherText.size)
                buffer.putInt(iv.size)
                buffer.put(iv)
                buffer.put(cipherText)
                buffer.flip()
                Base64Utils.encode(buffer.array())
            } else {
                null
            }
        } catch (e: Exception) {
            LogUtils.d("encryptByAes error:" + e.message)
        }
        return null
    }

    @JvmStatic
    fun decryptByAes(encryptedText: String?): String? {
        if (TextUtils.isEmpty(encryptedText)) {
            return ""
        }
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = secretKey
            val cipherMsg = Base64Utils.decode(encryptedText)

            //从密文字节流中提取认证标签和密文
            val buffer = cipherMsg?.let { ByteBuffer.wrap(it) } ?: return null
            val ivSize = buffer.getInt()
            val iv = ByteArray(ivSize)
            buffer[iv]
            val cipherText = ByteArray(buffer.remaining())
            buffer[cipherText]

            // 设置解密模式和GCM
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            return String(cipher.doFinal(cipherText))
        } catch (e: Exception) {
            LogUtils.d("decryptByAes error:" + e.message)
        }
        return null
    }

    @JvmStatic
    fun encryptByAesWithKey(plainText: String, key: String): String? {
        if (TextUtils.isEmpty(plainText) || TextUtils.isEmpty(key)) {
            return ""
        }
        try {
            val secretKeySpec = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), ALGORITHM)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val gcmParameterSpec = GCMParameterSpec(128, DEFAULT_IV)
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec)
            return Base64Utils.encode(cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8)))
        } catch (e: Exception) {
            LogUtils.d("encryptByAesWithKey error:" + e.message)
        }
        return null
    }

    @JvmStatic
    fun decryptByAesWithKey(encryptedText: String?, key: String): String? {
        if (TextUtils.isEmpty(encryptedText) || TextUtils.isEmpty(key)) {
            return ""
        }
        try {
            val secretKeySpec = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), ALGORITHM)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val gcmParameterSpec = GCMParameterSpec(128, DEFAULT_IV)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec)
            return String(cipher.doFinal(Base64Utils.decode(encryptedText)))
        } catch (e: Exception) {
            LogUtils.d("decryptByAesWithKey error:" + e.message)
        }
        return null
    }
}
