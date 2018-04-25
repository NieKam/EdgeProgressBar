package com.niekam.edgeprogressbar.indeterminate

/**
 * Copyright by Kamil Niezrecki
 */
interface EffectContract {

  fun getColors(): IntArray
  fun getLineWidth(): Float
  fun getTotalLength(): Float
  fun height(): Float
  fun invalidate()
  fun width(): Float
}