package com.bhh.tools.util

import android.content.Context
import android.os.ParcelFileDescriptor
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

private val logger = com.bhh.tools.log.Logger.get("File")

private fun createTempFile(context: Context): File {
    return File.createTempFile(
        "claw_temp_",
        ".dat",
        context.cacheDir // 使用应用缓存目录，避免权限问题
    ).apply {
        deleteOnExit() // JVM 退出时自动删除
    }
}

fun writeTempFile(context: Context, content: String): ParcelFileDescriptor {
    val tempFile = createTempFile(context).apply {
        writeText(content)
    }
    try {
        logger.info("写入临时大数据文件: ${tempFile.absolutePath}")
        return ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
    } finally {
        tempFile.delete()
    }
}

fun readTempFile(pfd: ParcelFileDescriptor): String? {
    var fis: FileInputStream? = null
    try {
        fis = FileInputStream(pfd.fileDescriptor)
        return fis.readBytes().toString(StandardCharsets.UTF_8)
    } catch (e: Exception) {
        logger.error(e.message?:"", e)
        return null
    } finally {
        fis?.close()
        pfd.close()
    }
}

fun File.copyFromAssets(context: Context, name: String): Boolean {
    context.assets.open(name).use { inputStream ->
        BufferedInputStream(inputStream).use { bufferedInputStream ->
            FileOutputStream(this.path).use { fileOutputStream ->
                BufferedOutputStream(fileOutputStream).use { bufferedOutputStream ->
                    return try {
                        bufferedInputStream.copyTo(bufferedOutputStream)
                        true
                    } catch (e: Exception) {
                        logger.error(e.message, e)
                        false
                    }
                }
            }
        }
    }
}

fun File.readFromAssets(context: Context): String? {
    return try {
        context.assets.open(name).use { inputStream ->
            BufferedInputStream(inputStream).use {
                it.readBytes().toString(StandardCharsets.UTF_8).also {
                    logger.info("成功读取 assets 文件: $name")
                }
            }
        }
    } catch (e: Exception) {
        logger.error("读取 assets 文件失败: ${e.message}", e)
        null
    }
}