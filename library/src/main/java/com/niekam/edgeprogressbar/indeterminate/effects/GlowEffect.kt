package com.niekam.edgeprogressbar.indeterminate.effects

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.MaskFilter
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import android.view.animation.LinearInterpolator
import com.niekam.edgeprogressbar.indeterminate.Effect
import com.niekam.edgeprogressbar.indeterminate.EffectContract
import com.niekam.edgeprogressbar.initLinePaint

class GlowEffect : Effect {
  private companion object {
    const val TAG = "Snake"
    const val GLOW_ANIMATION_DURATION_MS = 2000L
    const val END_RADIUS = 35F
  }

  private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

  private var mContract: EffectContract? = null
  private var mGlowAnimation: ValueAnimator? = null

  override fun onAttached(contract: EffectContract) {
    mContract = contract
    mPaint.initLinePaint(contract.getLineWidth(), contract.getColors()[0])
  }

  override fun onDetached() {
    stop()
    mContract = null
  }

  override fun onLineWidthChange(width: Float) {
    mPaint.strokeWidth = width
  }

  override fun onColorsChange(colors: IntArray) {
    mPaint.color = colors[0]
    mPaint.maskFilter = null
    reset()
  }

  override fun start() {
    require(mContract != null)

    if (isPending()) {
      Log.e(TAG, "Move animation already running. Exit")
      return
    }

    mGlowAnimation = ObjectAnimator.ofFloat(5F, END_RADIUS)
    mGlowAnimation?.repeatMode = ValueAnimator.REVERSE
    mGlowAnimation?.repeatCount = ValueAnimator.INFINITE
    mGlowAnimation?.duration = GLOW_ANIMATION_DURATION_MS
    mGlowAnimation?.interpolator = LinearInterpolator()
    mGlowAnimation?.addUpdateListener { it ->
      mPaint.maskFilter = getMask(it.animatedValue as Float)
      mContract?.invalidate()
    }

    mGlowAnimation?.start()
  }

  override fun stop() {
    if (!isPending()) {
      Log.e(TAG, "Indeterminate animation already stopped. Exit")
      return
    }

    mGlowAnimation?.cancel()
    mGlowAnimation = null
  }

  override fun onDraw(canvas: Canvas, path: Path) {
    canvas.drawPath(path, mPaint)
  }

  override fun onMeasure() {
    // Intentionally empty
  }

  private fun isPending(): Boolean {
    val anim = mGlowAnimation ?: return false
    return anim.isRunning
  }

  private fun getMask(radius: Float): MaskFilter? {
    if (radius == 0F) {
      return null
    }

    return BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
  }

  private fun reset() {
    if (isPending()) {
      stop()
      start()
    }
  }
}