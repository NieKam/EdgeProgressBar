package com.niekam.edgeprogressbar

import android.content.Context
import android.graphics.DashPathEffect
import android.os.Build

/**
 * Copyright by Kamil Niezrecki
 */
object Tools {

  private val DEFAULT_COLOR = R.color.default_stroke_color
  private val DEFAULT_STROKE_WIDTH = R.dimen.default_stroke_width

  @JvmStatic
  fun createPathEffect(pathLength: Float, phase: Float): DashPathEffect {
    return DashPathEffect(floatArrayOf(pathLength, pathLength), (phase * pathLength))
  }

  @JvmStatic
  fun getDefaultColor(context: Context): Int {
    return if (Build.VERSION.SDK_INT >= 23) {
      context.resources.getColor(DEFAULT_COLOR, null)
    } else {
      context.resources.getColor(DEFAULT_COLOR)
    }
  }

  @JvmStatic
  fun getDefaultStrokeWidth(context: Context): Float {
    return context.resources.getDimension(DEFAULT_STROKE_WIDTH)
  }
}