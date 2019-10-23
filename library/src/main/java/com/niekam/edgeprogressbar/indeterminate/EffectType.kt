package com.niekam.edgeprogressbar.indeterminate

import com.niekam.edgeprogressbar.indeterminate.effects.GlowEffect
import com.niekam.edgeprogressbar.indeterminate.effects.SnakeEffect
import com.niekam.edgeprogressbar.indeterminate.effects.ZigZagEffect

/**
 * Copyright by Kamil Niezrecki
 */
enum class EffectType(val effect: Effect) {
    ZIGZAG(ZigZagEffect()),
    SNAKE(SnakeEffect()),
    GLOW(GlowEffect());
}