package com.bhh.tools.robot

import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.bhh.tools.log.Logger
import org.json.JSONObject
import java.security.SecureRandom
import kotlin.math.abs

class Dagger(context: Context): ITouch(context) {

    /**
     * event:
     * MotionEvent.ACTION_CANCEL
     * MotionEvent.ACTION_UP
     */
    fun event(event: Int): Bundle? {
        val param = JSONObject()
        param.put("actionEvent", event)
        val actionName = "android_perform_event_action"
        val sequence = generateSequence()
        return callAction(actionName, param.toString(), sequence)
    }

    fun drag(fromX: Int, fromY: Int, toX: Int, toY: Int, isStart: Boolean, isEnd: Boolean, duration: Int): Bundle? {
        // 构造与 Python 对应的 JSON 参数
        logger.debug("gestureDragCoordinate: fromX=$fromX, fromY=$fromY, toX=$toX, toY=$toY, isStart=$isStart, isEnd=$isEnd, duration=$duration")
        val param = JSONObject()
            .put("fromX", fromX)
            .put("fromY", fromY)
            .put("toX", toX)
            .put("toY", toY)
            .put("duration", duration) //滑动时长，可选
            .put("isStart", isStart)//滑动开始
            .put("isEnd", isEnd);//滑动结束

        val actionName = "android_gesture_drag_coordinate"
        val sequence = generateSequence()

        return callAction(actionName, param.toString(), sequence)
    }

    fun swipe(fromX: Int, fromY: Int, toX: Int, toY: Int): Bundle?  {
        val param = JSONObject()
            .put("fromX", fromX)
            .put("fromY", fromY)
            .put("toX", toX)
            .put("toY", toY)
        val paramJson = param.toString()
        val action = "android_gesture_move_coordinate"
        val sequence = generateSequence()
        val result = callAction(action, paramJson, sequence)
        val success = result!!.getBoolean("success")
        val message = result.getString("msg")
        val data = result.getString("data")
//        logger.debug("swipe success: $success, msg: $message, data: $data")
        return result
    }
}