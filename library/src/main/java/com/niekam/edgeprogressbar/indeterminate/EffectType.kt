package com.niekam.edgeprogressbar.indeterminate

import com.niekam.edgeprogressbar.indeterminate.effects.ZigZagEffect

/**
 * Copyright by Kamil Niezrecki
 */
enum class EffectType(indeterminateEffect: Effect) {
  ZIGZAG(ZigZagEffect());

  val effect = indeterminateEffect
}