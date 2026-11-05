package com.bhh.tools.cypher

import android.annotation.SuppressLint
import org.apache.commons.codec.binary.Hex.decodeHex
import org.apache.commons.codec.binary.Hex.encodeHex
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

fun ByteArray.toHex() = String(encodeHex(this))

fun String.hexToByteArray() = decodeHex(this.toCharArray())!!

@SuppressLint("[ByDesign7.2]WeakEncryptionOrHashingAlgorithm")
fun ByteArray.toMD5(): ByteArray =
    MessageDigest.getInstance("MD5").digest(this)

fun String.toMD5() = this.toByteArray().toMD5().toHex()

fun String.isNumeric() = if (isEmpty()) false else !any { !it.isDigit() }

val File.md5: String
    @SuppressLint("[ByDesign7.2]WeakEncryptionOrHashingAlgorithm")
    get() {
        val md5 = MessageDigest.getInstance("MD5")
        forEachBlock { buffer, bytesRead ->
            md5.update(buffer, 0, bytesRead)
        }
        return md5.digest().toHex()
    }

fun UUID.toGUID() = toString().replace("-", "")

val numChars = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
val lowerCaseChars =
    charArrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z')
val upperCaseChars =
    charArrayOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')
val numLetterChars = charArrayOf(*numChars, *lowerCaseChars, *upperCaseChars)
val allChars = charArrayOf(
    *numLetterChars,
    '~',
    '`',
    '!',
    '@',
    '#',
    '$',
    '%',
    '^',
    '&',
    '*',
    '(',
    ')',
    '-',
    '_',
    '=',
    '+',
    '{',
    '[',
    ']',
    '}',
    '|',
    '\\',
    '\'',
    '"',
    '/',
    '?',
    '.',
    '>',
    '<',
    ','
)

val RANDOM = SecureRandom()

fun randomString(len: Int, vararg chars: Char): String {
    val maxLen = chars.size
    val b = StringBuilder()
    for (i in 0 until len) {
        b.append(chars[RANDOM.nextInt(maxLen)])
    }
    return b.toString()
}

fun randomString(len: Int): String = randomString(len, *allChars)

fun randomDigitsAndLetters(len: Int): String = randomString(len, *numLetterChars)

fun randomDigits(len: Int): String = randomString(len, *numChars)
