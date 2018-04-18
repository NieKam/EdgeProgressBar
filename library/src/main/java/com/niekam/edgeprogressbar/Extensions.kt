package com.niekam.edgeprogressbar

import android.content.res.Resources

/**
 * Converts dp to pixel
 */
val Int.dpToPx: Float get() = (this * Resources.getSystem().displayMetrics.density)

/**
 * Converts pixel to dp
 */
val Float.pxToDp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()