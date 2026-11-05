package com.bhh.tools.util

import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.TextView
import androidx.core.view.children


fun View.getViewInfo(): String{
    val buffer = StringBuffer()
    val resourceName= try {
        resources.getResourceName(id)
    }catch (e: Resources.NotFoundException){
        "null"
    }

    buffer.append("id: ").append(id)
    buffer.append("; resourceName: ").append(resourceName)
    buffer.append("; className: ").append(this::class.java.name)
    buffer.append("; contentDescription: ").append(contentDescription)
    if(this is TextView){
        buffer.append("; text: ").append(text)
        buffer.append("; hint: ").append(hint)
    } else if (this is ViewGroup) {
        children.forEach {
            buffer.append("\n")
            buffer.append(it.getViewInfo())
        }
    }
//    this.printObject()
    return buffer.toString()
}

fun View.isRectOverlap(referView: View): Boolean {
    val rect1 = Rect()
    val rect2 = Rect()

    // 获取两个视图在屏幕上的可见区域
    getGlobalVisibleRect(rect1)
    referView.getGlobalVisibleRect(rect2)
    // 检查矩形是否相交
    return rect1.intersect(rect2)
}

fun TextView.isContains(text: String, isContainText: Boolean = false, isContainDesc: Boolean = false): Boolean {
    var viewText = this.text.trim().toString()
    if (isContainDesc && text.isBlank()) {
        viewText = this.contentDescription.trim().toString()
    }
    if (isContainText) {
        return viewText.contains(text.trim()) || text.trim().contains(viewText)
    }
    return viewText == text.trim()
}
