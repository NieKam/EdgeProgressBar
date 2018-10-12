package com.niekam.edgeprogressbar

import com.niekam.edgeprogressbar.indeterminate.EffectType

interface EdgeProgressApi {

  fun setFirstColor(color: Int)

  fun setSecondColor(color: Int)

  fun setLineWidth(width: Int)

  fun getLineWidth(): Int

  fun setMax(max: Int)

  fun setProgress(progress: Float, animate: Boolean)

  fun getProgress(): Float

  fun setProgressAnimationDuration(duration: Long)

  fun setIndeterminate(isIndeterminate: Boolean)

  fun isIndeterminate(): Boolean

  fun setEffect(effect: EffectType)

  fun getEffectType(): EffectType?

  fun hide()

  fun show()
}