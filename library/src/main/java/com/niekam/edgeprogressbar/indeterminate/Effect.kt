package com.niekam.edgeprogressbar.indeterminate

import android.graphics.Canvas
import android.graphics.Path

/**
 * Copyright by Kamil Niezrecki
 */
interface Effect {

  fun onAttached(contract: EffectContract)
  fun onColorsChange(colors: IntArray)
  fun onDetached()
  fun onDraw(canvas: Canvas, path: Path)
  fun onLineWidthChange(width: Float)
  fun onMeasure()
  fun start()
  fun stop()
}