package com.niekam.edgeprogressbar.indeterminate

import android.graphics.Canvas

/**
 * Copyright by Kamil Niezrecki
 */
interface Effect {

  fun onAttached(contract: EffectContract)
  fun onColorsChange(colors: IntArray)
  fun onDetached()
  fun onDraw(canvas: Canvas)
  fun onLineWidthChange(width: Float)
  fun onMeasure()
  fun start()
  fun stop()
}