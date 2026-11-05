package com.bhh.tools.util

//import android.content.Context
//import com.google.android.gms.ads.identifier.AdvertisingIdClient
//import com.google.android.gms.common.GooglePlayServicesNotAvailableException
//import com.google.android.gms.common.GooglePlayServicesRepairableException
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID
import kotlin.random.Random
import kotlin.random.nextULong

object UniqueId {

    fun createAndroidId(): String {
        val id = java.lang.Long.toHexString(SecureRandom().nextLong())
        return id
    }

//    fun getGoogleAdId(context: Context): String? {
//        var adId: String? = null
//        try {
//            val info = AdvertisingIdClient.getAdvertisingIdInfo(context)
//            adId = info.id
//        } catch (e: GooglePlayServicesNotAvailableException) {
//            println("google play service not available")
//            // Google Play服务不可用
//        } catch (e: GooglePlayServicesRepairableException) {
//            println("google play service repairable")
//        } catch (e: IOException) {
//            println("google play service repairable")
//        } catch (e: Exception) {
//            // 其他未知错误
//        }
//        return adId
//    }

    fun createAdId(): String {
        return UUID.randomUUID().toString()
    }

    fun getBootId(): String {
        val file = File("/proc/sys/kernel/random/boot_id")
        val reader = FileReader(file)
        val buffer = CharArray(128)
        try {
            reader.read(buffer)
            return String(buffer).trim()
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        } finally {
            reader.close()
        }
    }

    fun generateBootId(): String {
        val segment1 = genRandomString(8)
        val segment2 = genRandomString(4)
        val segment3 = genRandomString(4)
        val segment4 = genRandomString(4)
        val segment5 = genRandomString(12)
        return "$segment1-$segment2-$segment3-$segment4-$segment5"
    }

    fun generateNewId(): String {
        // 生成16个随机的十六进制字符
        val part1 = genRandomString(16)
        // 生成16个随机的十六进制字符
        val part2 = genRandomString(16)
        // 组合两部分并添加分隔符
        return "$part1-$part2"
    }

    fun generateToken175(): String {
        // 生成16个随机的十六进制字符
        val randomULong: ULong = Random.nextULong()
        return randomULong.toString()
    }

    fun generateToken258(): String {
        // 生成16个随机的十六进制字符
        return genRandomString(64)
    }

    fun generateToken281(): String {
        // 生成16个随机的十六进制字符
        val part1 = UUID.randomUUID().toString()
        return "1500:$part1"
    }

    fun genRandomString(length: Int): String {
        val random = SecureRandom()
        val sb = StringBuilder(length)
        for (i in 0 until length) {
            val randomInt = random.nextInt(16)
            val hexChar = if (randomInt < 10) {
                '0' + randomInt
            } else {
                'a' + (randomInt - 10)
            }
            sb.append(hexChar)
        }
        return sb.toString()
    }

    fun getKernalUUID(): String {
        val file = File("/proc/sys/kernel/random/uuid")
        val reader = FileReader(file)
        val buffer = CharArray(128)
        try {
            reader.read(buffer)
            return String(buffer).trim()
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        } finally {
            reader.close()
        }
    }

    fun generateRandomSHA256Hash(): String {
        // 创建一个随机字节数组
        val randomBytes = ByteArray(32)
        SecureRandom().nextBytes(randomBytes)

        // 创建一个MessageDigest实例
        val digest = MessageDigest.getInstance("SHA-256")

        // 计算字节数组的哈希值
        val hashBytes = digest.digest(randomBytes)

        // 将字节数组转换为十六进制字符串
        val hexString = hashBytes.joinToString("") { "%02x".format(it) }

        return hexString
    }

    fun generateDrmId(): String {
        val random = SecureRandom()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun generateRandomString(length: Int): String {
        val charset = "0123456789abcdef" // 你可以根据需要修改字符集
        val random = SecureRandom()
        val result = StringBuilder(length)

        for (i in 0 until length) {
            val randomIndex = random.nextInt(charset.length)
            result.append(charset[randomIndex])
        }

        return result.toString()
    }


    fun getUUIDByteArray(): ByteArray {
        return getByteArrayFromUUID(UUID.randomUUID())
    }

    fun getByteArrayFromUUID(uuid: UUID): ByteArray {
        val msb = uuid.mostSignificantBits
        val lsb = uuid.leastSignificantBits

        val uuidBytes = ByteArray(16)
        for (i in 0 until 8) {
            uuidBytes[i] = (msb shr (8 * (7 - i))).toByte()
            uuidBytes[8 + i] = (lsb shr (8 * (7 - i))).toByte()
        }
        return uuidBytes
    }


}