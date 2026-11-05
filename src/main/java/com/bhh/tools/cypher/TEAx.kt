package com.bhh.tools.cypher

import kotlin.experimental.xor

class TEAx(key: ByteArray) {

    companion object {
        private const val TEA_DELTA = 0x9e3779b9L // Magic number for key schedule
        private const val TEA_BLOCK_SIZE = 8

        fun encode(key: ByteArray, clear: ByteArray) = TEAx(key).encode(clear)

        fun decode(key: ByteArray, cipher: ByteArray) = TEAx(key).decode(cipher)

        fun encode(key: String, clear: String) =
            encode(key.toByteArray(), clear.toByteArray()).toHex()

        fun decode(key: String, cipher: String) =
            String(decode(key.toByteArray(), cipher.hexToByteArray()))

    }

    private val k0: Long
    private val k1: Long
    private val k2: Long
    private val k3: Long

    private val subKey: ByteArray

    init {
        if (key.isEmpty()) {
            throw IllegalArgumentException("empty key is not supported")
        }

        val md5value = key.toMD5()
        val k = LongArray(4)
        for (i in 0..3) {
            for (j in 0..3) {
                // java 总是big endian的
                var temp = (md5value[i * 4 + j] + 256 and 0xff).toLong()
                temp = temp shl (8 * j)
                k[i] = k[i] or temp
            }
        }
        k0 = k[0]
        k1 = k[1]
        k2 = k[2]
        k3 = k[3]
        // 第二次md5作为subKey
        subKey = md5value.toMD5()
    }


    fun encode(clear: ByteArray): ByteArray {

        val coded = ByteArray(clear.size)

        var bytesLeft = clear.size

        while (bytesLeft >= TEA_BLOCK_SIZE) {

            encodeBlock(clear, coded, clear.size - bytesLeft)

            bytesLeft -= TEA_BLOCK_SIZE
        }

        if (bytesLeft > 0)
            subCrypt(clear, coded, clear.size - bytesLeft)

        return coded

    }

    fun decode(coded: ByteArray): ByteArray {

        val clear = ByteArray(coded.size)


        var bytesLeft = coded.size

        while (bytesLeft >= TEA_BLOCK_SIZE) {

            decodeBlock(coded, clear, coded.size - bytesLeft)

            bytesLeft -= TEA_BLOCK_SIZE
        }

        if (bytesLeft > 0) {
            subCrypt(coded, clear, coded.size - bytesLeft)
        }

        return clear
    }

    private fun encodeBlock(clear: ByteArray, coded: ByteArray, offset: Int) {

        var y: Long = 0
        for (i in 0..3) {
            var temp = (clear[offset + i] + 256 and 0xff).toLong()
            temp = temp shl (8 * (3 - i))
            y = y or temp
        }

        var z: Long = 0
        for (i in 0..3) {
            var temp = (clear[offset + 4 + i] + 256 and 0xff).toLong()
            temp = temp shl (8 * (3 - i))
            z = z or temp
        }

        var sum: Long = 0
        for (count in 32 downTo 1) {

            sum += TEA_DELTA // Magic number for key schedule
            sum = sum and 0xffffffffL
            y += (z shl 4) + k0 xor z + sum xor (z shr 5) + k1
            y = y and 0xffffffffL
            z += (y shl 4) + k2 xor y + sum xor (y shr 5) + k3 /* end cycle */
            z = z and 0xffffffffL
        }

        for (i in 0..3)
            coded[offset + i] = (y shr 8 * (3 - i) and 0xff).toByte()

        for (i in 0..3)
            coded[offset + 4 + i] = (z shr 8 * (3 - i) and 0xff).toByte()

    }

    private fun decodeBlock(coded: ByteArray, clear: ByteArray, offset: Int) {

        var y: Long = 0
        for (i in 0..3) {

            var temp = (coded[offset + i] + 256 and 0xff).toLong()
            temp = temp shl (8 * (3 - i))
            y = y or temp
        }

        var z: Long = 0
        for (i in 0..3) {

            var temp = (coded[offset + 4 + i] + 256 and 0xff).toLong()
            temp = temp shl (8 * (3 - i))
            z = z or temp
        }

        var sum = TEA_DELTA shl 5
        sum = sum and 0xffffffffL
        for (count in 32 downTo 1) {
            z += 0x100000000L
            z -= (y shl 4) + k2 xor y + sum xor (y shr 5) + k3 and 0xffffffffL
            z = z and 0xffffffffL
            y += 0x100000000L
            y -= (z shl 4) + k0 xor z + sum xor (z shr 5) + k1 and 0xffffffffL
            y = y and 0xffffffffL
            sum += 0x100000000L
            sum -= TEA_DELTA // Magic number for key schedule
            sum = sum and 0xffffffffL

        }

        for (i in 0..3)
            clear[offset + i] = (y shr 8 * (3 - i) and 0xff).toByte()

        for (i in 0..3)
            clear[offset + 4 + i] = (z shr 8 * (3 - i) and 0xff).toByte()
    }

    private fun subCrypt(input: ByteArray, output: ByteArray, offset: Int) {

        // do sub encrypt
        for (i in 0 until input.size - offset) {

            if (subKey.isNotEmpty()) {
                var temp = subKey[(i + input.size) % subKey.size] + 256 and 0xff
                temp %= subKey.size
                temp = subKey[temp] + 256 and 0xff

                output[i + offset] = (input[i + offset] xor temp.toByte())
            } else {
                output[i + offset] = input[i + offset]
            }
        }
    }

}
