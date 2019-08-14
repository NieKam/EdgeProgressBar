package com.niekam.edgeprogressbar.indeterminate

import com.niekam.edgeprogressbar.indeterminate.effects.GlowEffect
import com.niekam.edgeprogressbar.indeterminate.effects.SnakeEffect
import com.niekam.edgeprogressbar.indeterminate.effects.ZigZagEffect

/**
 * Copyright by Kamil Niezrecki
 */
enum class EffectType(indeterminateEffect: Effect) {
    ZIGZAG(ZigZagEffect()),
    SNAKE(SnakeEffect()),
    GLOW(GlowEffect());

    val effect = indeterminateEffect
}