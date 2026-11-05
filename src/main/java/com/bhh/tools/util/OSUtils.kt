package com.bhh.tools.util

import android.text.TextUtils
import java.util.Locale

object OSUtils {
    private const val KEY_DISPLAY: String = "ro.build.display.id"
    private const val KEY_EMUI_VERSION_NAME: String = "ro.build.version.emui"
    private const val KEY_MIUI_VERSION_NAME: String = "ro.miui.ui.version.name"

    val eMUIVersion: String?
        get() = if (isEMUI) getSystemProperty(
            KEY_EMUI_VERSION_NAME,
            ""
        ) else ""

    private val flymeOSFlag: String?
        get() = getSystemProperty(
            KEY_DISPLAY,
            ""
        )

    val flymeOSVersion: String?
        get() {
            return if (isFlymeOS) getSystemProperty(
                KEY_DISPLAY,
                ""
            ) else ""
        }

    val mIUIVersion: String?
        get() {
            return if (isMIUI) getSystemProperty(
                KEY_MIUI_VERSION_NAME,
                ""
            ) else ""
        }

    private fun getSystemProperty(str: String, str2: String): String? {
        try {
            val cls: Class<*> = Class.forName("android.os.SystemProperties")
            return cls.getMethod("get", String::class.java, String::class.java)
                .invoke(cls, str, str2) as String
        } catch (e: Exception) {
            e.printStackTrace()
            return str2
        }
    }

    val isEMUI: Boolean
        get() {
            return !TextUtils.isEmpty(
                getSystemProperty(
                    KEY_EMUI_VERSION_NAME,
                    ""
                )
            )
        }

    val isEMUI3_0: Boolean
        get() {
            return eMUIVersion!!.contains("EmotionUI_3.0")
        }

    val isEMUI3_1: Boolean
        get() {
            val eMUIVersion: String? = eMUIVersion
            return "EmotionUI 3" == eMUIVersion || eMUIVersion!!.contains("EmotionUI_3.1")
        }

    val isEMUI3_x: Boolean
        get() {
            return isEMUI3_0 || isEMUI3_1
        }

    val isFlymeOS: Boolean
        get() {
            return flymeOSFlag!!.lowercase(Locale.getDefault())
                .contains("flyme")
        }

    val isFlymeOS4Later: Boolean
        get() {
            val intValue: Int
            val flymeOSVersion: String? = flymeOSVersion
            if (flymeOSVersion!!.isEmpty()) {
                return false
            }
            try {
                if (flymeOSVersion.lowercase(Locale.getDefault()).contains("os")) {
                    intValue = flymeOSVersion.substring(9, 10).toInt()
                } else {
                    intValue = flymeOSVersion.substring(6, 7).toInt()
                }
                return intValue >= 4
            } catch (unused: NumberFormatException) {
                return false
            }
        }

    val isFlymeOS5: Boolean
        get() {
            val intValue: Int
            val flymeOSVersion: String? = flymeOSVersion
            if (flymeOSVersion!!.isEmpty()) {
                return false
            }
            try {
                if (flymeOSVersion.lowercase(Locale.getDefault()).contains("os")) {
                    intValue = flymeOSVersion.substring(9, 10).toInt()
                } else {
                    intValue = flymeOSVersion.substring(6, 7).toInt()
                }
                return intValue == 5
            } catch (unused: NumberFormatException) {
                return false
            }
        }

    val isMIUI: Boolean
        get() {
            return !TextUtils.isEmpty(
                getSystemProperty(
                    KEY_MIUI_VERSION_NAME,
                    ""
                )
            )
        }

    val isMIUI6Later: Boolean
        get() {
            val mIUIVersion: String? = mIUIVersion
            if (mIUIVersion!!.isEmpty()) {
                return false
            }
            try {
                return mIUIVersion.substring(1).toInt() >= 6
            } catch (unused: NumberFormatException) {
                return false
            }
        }

    val isLineageOS: Boolean
        get() {
            return !TextUtils.isEmpty(
                getSystemProperty(
                    "ro.lineage.version",
                    ""
                )
            )
        }
}
