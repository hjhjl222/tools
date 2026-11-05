package com.bhh.tools.cypher

import java.util.*

class TEAxPlus(private val key: String) {

    companion object {
        fun encode(key: String, clear: String) = TEAxPlus(key).encode(clear)

        fun decode(key: String, cipher: String) = TEAxPlus(key).decode(cipher)

        fun encode(key: String, clear: ByteArray) = TEAxPlus(key).encode(clear)

        fun decode(key: String, cipher: ByteArray) = TEAxPlus(key).decode(cipher)

        fun encode(prefix: String, key: String, clear: String): ByteArray {
            val prefixByteArray = prefix.toByteArray()
            val cipherByteArray = encode(key, clear.toByteArray())
            val result = ByteArray(prefixByteArray.size + cipherByteArray.size)
            System.arraycopy(prefixByteArray, 0, result, 0, prefixByteArray.size)
            System.arraycopy(cipherByteArray, 0, result, prefixByteArray.size, cipherByteArray.size)
            return result
        }

        fun decode(prefix: String, key: String, cipher: ByteArray): ByteArray {
            var temp = cipher
            val prefixByteArray = prefix.toByteArray()
            if (prefixByteArray.size > temp.size) {
                return ByteArray(0)
            }
            if (prefixByteArray.indices.any { temp[it] != prefixByteArray[it] }) {
                return ByteArray(0)
            }
            temp = Arrays.copyOfRange(temp, prefixByteArray.size, temp.size)
            return decode(key, temp)
        }

    }


    fun decode(cipher: String): String {
        var temp = cipher
        if (temp.length < 64) {
            return ""
        }
        val md5 = temp.substring(0, 32)
        val randomKey = temp.substring(32, 64)
        temp = temp.substring(64)
        val clear = TEAx.decode(randomKey + key, temp)
        return if (md5 == clear.toMD5()) {
            clear
        } else {
            ""
        }
    }

    fun encode(clear: String, randomKey: String = RANDOM.nextLong().toString()): String {
        val md5 = clear.toMD5().uppercase(Locale.getDefault())
        val randKey = randomKey.toMD5()
        return md5 + randKey + TEAx.encode(randKey + key, clear)
    }

    fun decode(cipher: ByteArray): ByteArray {
        if (cipher.size < 32) {
            return ByteArray(0)
        }
        var temp = cipher

        val md5Bytes = Arrays.copyOf(temp, 16)
        val randomKeyBytes = Arrays.copyOfRange(temp, 16, 32)
        temp = Arrays.copyOfRange(temp, 32, temp.size)

        val randomKey = randomKeyBytes.toHex()
        val clearByteArray = TEAx.decode((randomKey + key).toByteArray(), temp)
        val md5ByteArray = clearByteArray.toMD5()
        if (md5ByteArray.indices.any { md5ByteArray[it] != md5Bytes[it] }) {
            return ByteArray(0)
        }

        return clearByteArray
    }

    fun encode(clear: ByteArray, randomKey: ByteArray = RANDOM.nextLong().toString().toByteArray()): ByteArray {
        val md5 = clear.toMD5()
        val randKey = randomKey.toMD5()
        val coded = TEAx.encode((randKey.toHex() + key).toByteArray(), clear)
        val result = ByteArray(md5.size + randKey.size + coded.size)
        System.arraycopy(md5, 0, result, 0, md5.size)
        System.arraycopy(randKey, 0, result, md5.size, randKey.size)
        System.arraycopy(coded, 0, result, md5.size + randKey.size, coded.size)
        return result
    }
}
