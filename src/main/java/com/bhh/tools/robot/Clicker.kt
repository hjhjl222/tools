package com.bhh.tools.robot

import android.content.Context
import android.os.Bundle
import org.json.JSONObject

class Clicker(context: Context): ITouch(context) {

    fun longClick(x: Int, y: Int) = click(x, y, duration = 500)

    fun click(x: Int, y: Int, duration: Int = 20): Bundle? {
        val param = JSONObject()
            .put("x", x)
            .put("y", y)
            .put("duration", duration)
        val paramJson = param.toString()
        val action = "android_gesture_click_coordinate"
        val sequence = generateSequence()
        return callAction(action, paramJson, sequence)
    }
}