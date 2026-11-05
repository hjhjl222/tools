package com.bhh.tools.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Base64
import java.io.File
import java.security.SecureRandom

private const val RANDOM_DIR_PREFIX = "~~"
private const val RANDOM_CODEPATH_PREFIX: Char = '-';

object PackageInfoUtil {

    fun getNextCodePath(targetDir: File, packageName: String): File {
        val random = SecureRandom()
        val bytes = ByteArray(16)
        var firstLevelDir: File
        do {
            random.nextBytes(bytes)
            val firstLevelDirName = RANDOM_DIR_PREFIX + Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP).replace("\\u003d\\u003d","==")
            firstLevelDir = File(targetDir, firstLevelDirName)
        } while (firstLevelDir.exists())
        random.nextBytes(bytes)
        val dirName = packageName + RANDOM_CODEPATH_PREFIX + Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP).replace("\\u003d\\u003d","==")
        val result = File(firstLevelDir, dirName)
        return result
    }

    fun createShortCodePath(): String {
        val random = SecureRandom()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        return RANDOM_DIR_PREFIX + Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP).replace("\\u003d\\u003d","==")
    }

    fun getDefaultLauncher(context: Context): String? {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        return context.packageManager?.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo?.packageName
    }
}