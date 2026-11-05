package com.bhh.tools.cypher

import android.graphics.Color
import java.util.Locale

object Digital {

    fun toHex(r: Int, g: Int, b: Int): String {
        return ("#" + toBrowserHexValue(r) + toBrowserHexValue(g) + toBrowserHexValue(b))
    }

    fun toHex(alpha: Int, r: Int, g: Int, b: Int): String {
        return ("#" + toBrowserHexValue(alpha) + toBrowserHexValue(r) + toBrowserHexValue(g) + toBrowserHexValue(b))
    }

    fun changeAlpha(color: Int, fraction: Float): Int {
        val red: Int = Color.red(color)
        val green: Int = Color.green(color)
        val blue: Int = Color.blue(color)
        val alpha = (Color.alpha(color) * fraction).toInt()
        return Color.argb(alpha, red, green, blue)
    }

    private fun toBrowserHexValue(number: Int): String {
        val builder = StringBuilder(
            Integer.toHexString(number and 0xff)
        )
        while (builder.length < 2) {
            builder.append("0")
        }
        return builder.toString().uppercase(Locale.getDefault())
    }

    fun toTwoDecimals(num: Double): String {
        return String.format("%.2f", num)
    }
}