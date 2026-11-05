package com.bhh.tools.util

import android.util.Base64
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

fun String.toHex(): String {
    return this.toByteArray().joinToString("") {
        "%02x".format(it)
    }
}

fun String.compress(): String {
    val data = this.toByteArray(Charsets.UTF_8)
    val deflater = Deflater(Deflater.BEST_COMPRESSION)
    deflater.setInput(data)
    deflater.finish()

    val outputStream = ByteArrayOutputStream(data.size)
    val buffer = ByteArray(1024)
    while (!deflater.finished()) {
        val count = deflater.deflate(buffer)
        outputStream.write(buffer, 0, count)
    }
    outputStream.close()
    return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
}

// 添加字符串解压扩展函数
fun String.decompress(): String {
    val data = Base64.decode(this, Base64.DEFAULT)
    val inflater = Inflater()
    inflater.setInput(data)

    val outputStream = ByteArrayOutputStream(data.size)
    val buffer = ByteArray(1024)
    while (!inflater.finished()) {
        val count = inflater.inflate(buffer)
        outputStream.write(buffer, 0, count)
    }
    outputStream.close()
    return String(outputStream.toByteArray(), Charsets.UTF_8)
}