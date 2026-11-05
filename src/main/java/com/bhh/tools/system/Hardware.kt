package com.bhh.tools.system

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.telephony.TelephonyManager
import android.text.format.Formatter
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.ViewConfiguration
import com.bhh.tools.app.IApplicationHandler
import java.io.File

@Suppress("DEPRECATION")
@SuppressLint("MissingPermission", "HardwareIds")
object Hardware : IApplicationHandler {

    /**
     * 手机的型号 设备名称
     */
    val model = Build.MODEL!!

    /**
     * 设备品牌
     */
    val brand = Build.BRAND!!

    /**
     * 设备制造商
     */
    val manufacturer = Build.MANUFACTURER!!

    /**
     * 整个产品的名称
     */
    val product = Build.PRODUCT!!

    /**
     * 设备代号
     */
    val device = Build.DEVICE!!

    val sdk = Build.VERSION.SDK_INT

    val imei: String? by lazy {
        val telephonyManager = application!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            telephonyManager.imei
        } else {
            telephonyManager.deviceId
        }
    }

    val imsi: String? by lazy {
        val telephonyManager = application!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.subscriberId
    }

    val phoneNumber: String? by lazy {
        val telephonyManager = application!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.line1Number
    }

    val totalRam: Long by lazy {
        /*val line = File("/proc/meminfo")
            .readLines()
            .find {
                it.startsWith("MemTotal")
            } ?: return@lazy ""
        val size = line.substringBeforeLast(" ")
            .substringAfterLast(" ")
            .toLong() * 1024L
        Formatter.formatFileSize(application, size)*/
        val activityManager = application!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        memoryInfo.totalMem
    }

    val availRam: String
        get() {
            val activityManager = application!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            return Formatter.formatFileSize(application, memoryInfo.availMem)
        }

    val totalRom: Long by lazy {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)

        val blockSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            stat.blockSizeLong
        } else {
            stat.blockSize.toLong()
        }
        val totalBlocks = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            stat.blockCountLong
        } else {
            stat.blockCount.toLong()
        }
        totalBlocks
        /*Formatter.formatFileSize(application, totalBlocks * blockSize)*/
    }

    val availRom: String
        get() {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)

            val blockSize =
                stat.blockSizeLong
            val totalBlocks =
                stat.availableBlocksLong

            return Formatter.formatFileSize(application, totalBlocks * blockSize)
        }

    val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId = application!!.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = application!!.resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

    /**
     * 获取NavigationBar的高度
     * @return
     */
    val navigationBarHeight: Int
        get() {
            if (hasNavigationBar) {
                val resources = application!!.resources
                val resourceId = resources.getIdentifier(
                    "navigation_bar_height",
                    "dimen", "android"
                )
                //获取NavigationBar的高度
                return resources.getDimensionPixelSize(resourceId)
            }
            return 0
        }

    /**
     * 判断是否有NavigationBar
     * @return
     */
    val hasNavigationBar: Boolean
        get() {
            //通过判断设备是否有返回键、菜单键(不是虚拟键,是手机屏幕外的按键)来确定是否有navigation bar
            val hasMenuKey = ViewConfiguration.get(application!!)
                .hasPermanentMenuKey()
            val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
            return !hasMenuKey && !hasBackKey
        }


    data class Resolution(val width: Int, val height: Int, val densityDpi: Int)

    val displayResolution: Resolution by lazy {
        val display = application!!.resources.displayMetrics
        Resolution(display.widthPixels, display.heightPixels + statusBarHeight + navigationBarHeight, display.densityDpi)
    }


    data class Network(val type: String, val name: String?)

    private val SUB_TYPE_2G = intArrayOf(
        TelephonyManager.NETWORK_TYPE_GPRS,
        TelephonyManager.NETWORK_TYPE_EDGE,
        TelephonyManager.NETWORK_TYPE_CDMA,
        TelephonyManager.NETWORK_TYPE_1xRTT,
        TelephonyManager.NETWORK_TYPE_IDEN
    )

    private val SUB_TYPE_3G = intArrayOf(
        TelephonyManager.NETWORK_TYPE_UMTS,
        TelephonyManager.NETWORK_TYPE_EVDO_0,
        TelephonyManager.NETWORK_TYPE_EVDO_A,
        TelephonyManager.NETWORK_TYPE_HSDPA,
        TelephonyManager.NETWORK_TYPE_HSUPA,
        TelephonyManager.NETWORK_TYPE_HSPA,
        TelephonyManager.NETWORK_TYPE_EVDO_B,
        TelephonyManager.NETWORK_TYPE_EHRPD,
        TelephonyManager.NETWORK_TYPE_HSPAP
    )

    private val SUB_TYPE_4G = intArrayOf(TelephonyManager.NETWORK_TYPE_LTE)

    private fun getSubTypeName(subType: Int): String {
        return when (subType) {
            TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
            TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
            TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
            TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
            TelephonyManager.NETWORK_TYPE_IDEN -> "iDen"
            TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
            TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
            TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
            TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
            TelephonyManager.NETWORK_TYPE_EHRPD -> "eHRPD"
            TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
            TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO"
            TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO"
            TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO"
            TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "TD_SCDMA"
            TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
            else -> "unknown"
        }
    }

    val network: Network?
        get() {
            val manager = application!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = manager.activeNetworkInfo ?: return null
            return when (networkInfo.type) {
                ConnectivityManager.TYPE_WIFI -> Network("WIFI", networkInfo.extraInfo?.let {
                    if (it.startsWith("\"") && it.endsWith("\"")) {
                        it.substringAfter('"').substringBefore('"')
                    } else {
                        it
                    }
                })
                ConnectivityManager.TYPE_MOBILE -> {
                    when (networkInfo.subtype) {
                        in SUB_TYPE_2G -> Network("2G", getSubTypeName(networkInfo.subtype))
                        in SUB_TYPE_3G -> Network("3G", getSubTypeName(networkInfo.subtype))
                        in SUB_TYPE_4G -> Network("4G", getSubTypeName(networkInfo.subtype))
                        else -> null
                    }
                }
                else -> null
            }
        }

    val isNetworkAvail: Boolean
        @SuppressLint("[ByDesign12.1]UsingRuntimeExec")
        get() {
            val runtime = Runtime.getRuntime()
            val p = runtime.exec("ping -c 3 www.baidu.com")
            return p.waitFor() == 0
        }
}