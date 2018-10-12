package com.niekam.edgeprogressbar.indeterminate

import android.graphics.Canvas
import android.graphics.Path

/**
 * Copyright by Kamil Niezrecki
 */
interface Effect {

  fun onAttached(contract: EffectContract)
  fun onDetached()
  fun onPrimaryColorChange(color: Int)
  fun onSecondaryColorChange(color: Int)
  fun onDraw(canvas: Canvas, path: Path)
  fun onLineWidthChange(width: Float)
  fun onMeasure()
}