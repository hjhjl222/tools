package com.bhh.tools.cypher

import kotlin.experimental.and
import kotlin.experimental.xor

class RC4(key: String) {

    private val key = initKey(key)

    companion object {
        fun encode(key: String, clear: ByteArray): ByteArray {
            return RC4(key).cypher(clear)
        }

        fun decode(key: String, cipher: ByteArray): ByteArray {
            return RC4(key).cypher(cipher)
        }

        fun encode(key: String, clear: String): String {
            return RC4(key).cypher(clear.toByteArray()).toHex()
        }

        fun decode(key: String, cipher: String): String {
            return String(RC4(key).cypher(cipher.hexToByteArray()))
        }
    }

    private fun initKey(key: String): ByteArray {
        val keyBytes = if (key.isEmpty()) {
            "123456".toByteArray()
        } else {
            key.toByteArray()
        }
        val state = ByteArray(256)

        for (i in 0..255) {
            state[i] = i.toByte()
        }
        var index1 = 0
        var index2 = 0

        for (i in 0..255) {
            index2 = (keyBytes[index1] and 0xff.toByte()) + (state[i] and 0xff.toByte()) + index2 and 0xff
            val tmp = state[i]
            state[i] = state[index2]
            state[index2] = tmp
            index1 = (index1 + 1) % keyBytes.size
        }
        return state
    }

    fun cypher(input: ByteArray): ByteArray {
        var x = 0
        var y = 0
        var xorIndex: Int
        val result = ByteArray(input.size)

        for (i in input.indices) {
            x = x + 1 and 0xff
            y = (key[x] and 0xff.toByte()) + y and 0xff
            val tmp = key[x]
            key[x] = key[y]
            key[y] = tmp
            xorIndex = (key[x] and 0xff.toByte()) + (key[y] and 0xff.toByte()) and 0xff
            result[i] = (input[i] xor key[xorIndex])
        }
        return result
    }
}