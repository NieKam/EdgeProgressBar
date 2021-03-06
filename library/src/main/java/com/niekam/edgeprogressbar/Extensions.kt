package com.niekam.edgeprogressbar

import android.content.res.Resources
import android.graphics.Paint
import android.os.Build
import com.niekam.edgeprogressbar.Constants.DEFAULT_COLOR
import com.niekam.edgeprogressbar.Constants.DEFAULT_STROKE_WIDTH

/**
 * Converts dp to pixel
 */
val Int.dpToPx: Float get() = (this * Resources.getSystem().displayMetrics.density)

/**
 * Converts pixel to dp
 */
val Float.pxToDp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()

fun Resources.getDefaultStrokeWidth(): Float {
    return getDimension(DEFAULT_STROKE_WIDTH)
}

fun Resources.getDefaultColor(): Int {
    return if (Build.VERSION.SDK_INT >= 23) {
        getColor(DEFAULT_COLOR, null)
    } else {
        getColor(DEFAULT_COLOR)
    }
}

fun Paint.initLinePaint(lineWidth: Float, color: Int) {
    this.color = color
    this.style = Paint.Style.STROKE
    this.strokeWidth = lineWidth
    this.isDither = true
    this.strokeJoin = Paint.Join.ROUND
    this.strokeCap = Paint.Cap.ROUND
}