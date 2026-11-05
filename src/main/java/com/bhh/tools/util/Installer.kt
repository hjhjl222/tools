package com.bhh.tools.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.InstallSourceInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

object Installer {

    fun getInstallSourceInfo(context: Context, packageName: String): InstallSourceInfo? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                context.packageManager.getInstallSourceInfo(packageName)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun getInstalledApplications(context: Context): List<ApplicationInfo> {
        return context.packageManager.getInstalledApplications(0)
    }

    fun getApplicationInfo(context: Context, packageName: String): ApplicationInfo? {
        return try {
            context.packageManager.getApplicationInfo(packageName, 0)
        } catch (e: Exception) {
            null
        }
    }

    fun getInstalledPackages(context: Context): List<PackageInfo> {
        return context.packageManager.getInstalledPackages(0)
    }

    fun getPackageInfo(context: Context, packageName: String, flags: Int = 0): PackageInfo? {
        return try {
            context.packageManager.getPackageInfo(packageName, flags)
        } catch (e: Exception) {
            null
        }
    }
}