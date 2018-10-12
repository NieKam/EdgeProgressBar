package com.niekam.edgeprogressbar.indeterminate

/**
 * Copyright by Kamil Niezrecki
 */

interface EffectContract {

  fun getFirstColor(): Int
  fun getSecondaryColor(): Int
  fun getLineWidthInPx(): Float
  fun getTotalLineLength(): Float
  fun height(): Float
  fun width(): Float
  fun requestInvalidate()
}