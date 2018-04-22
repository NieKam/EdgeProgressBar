package com.niekam.edgeprogressbar.effects

import android.graphics.Canvas

/**
 * Copyright by Kamil Niezrecki
 */
interface Effect {

  fun onDraw(canvas: Canvas)
  fun onSizeChanged(width: Int, height: Int)
  fun start()
  fun stop()
  fun isPending(): Boolean
  fun setStrokeWidth(width: Int)
}