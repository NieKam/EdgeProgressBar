package com.niekam.edgeprogressbar.indeterminate.effects

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.Log
import android.view.animation.LinearInterpolator
import com.niekam.edgeprogressbar.Constants.END_GRADIENT
import com.niekam.edgeprogressbar.indeterminate.Effect
import com.niekam.edgeprogressbar.indeterminate.EffectContract
import com.niekam.edgeprogressbar.initLinePaint

/**
 * Copyright by Kamil Niezrecki
 */
class ZigZagEffect : Effect {
  private companion object {
    const val TAG = "ZigZag"
    const val PATH_DASH_SEGMENTS = 6
    const val DASH_LINE_COUNT = 2
    const val INDETERMINATE_ANIMATION_DURATION_MS = 4000L
  }

  private val mDashPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private var mContract: EffectContract? = null
  private var mIndeterminateAnimation: ValueAnimator? = null
  private var mLineSegmentSize: Float = 0F

  override fun onAttached(contract: EffectContract) {
    contract.let {
      mContract = it
      mPaint.initLinePaint(it.getLineWidthInPx(), it.getFirstColor())
      mDashPaint.initLinePaint(it.getLineWidthInPx(), it.getSecondaryColor())
    }

    start()
  }

  override fun onDetached() {
    stop()
    mContract = null
  }

  override fun onLineWidthChange(width: Float) {
    mPaint.strokeWidth = width
    mDashPaint.strokeWidth = width
  }

  override fun onPrimaryColorChange(color: Int) {
    mContract?.let {
      mPaint.shader = getShader(color, it.getSecondaryColor())
    }

    reset()
  }

  override fun onSecondaryColorChange(color: Int) {
    mContract?.let {
      mPaint.shader = getShader(it.getFirstColor(), color)
    }
    reset()
  }

  private fun start() {
    require(mContract != null)
    val contract = mContract as EffectContract

    if (isPending()) {
      Log.e(TAG, "Indeterminate animation already running. Exit")
      return
    }

    mIndeterminateAnimation = ObjectAnimator.ofFloat(
        (contract.getTotalLineLength() * DASH_LINE_COUNT),
        0F)
    mIndeterminateAnimation?.repeatCount = ValueAnimator.INFINITE
    mIndeterminateAnimation?.duration = INDETERMINATE_ANIMATION_DURATION_MS
    mIndeterminateAnimation?.interpolator = LinearInterpolator()
    mIndeterminateAnimation?.addUpdateListener { it ->
      mDashPaint.pathEffect = DashPathEffect(
          floatArrayOf(mLineSegmentSize, mLineSegmentSize),
          it.animatedValue as Float)
      mContract?.requestInvalidate()
    }
    mIndeterminateAnimation?.start()

    val argbEvaluator = ArgbEvaluator()
    mDashPaint.color = argbEvaluator.evaluate(.65F, contract.getFirstColor(), contract.getSecondaryColor()) as Int
  }

  private fun stop() {
    if (!isPending()) {
      Log.e(TAG, "Indeterminate animation already stopped. Exit")
      return
    }

    mIndeterminateAnimation?.cancel()
    mIndeterminateAnimation = null
  }

  override fun onDraw(canvas: Canvas, path: Path) {
    canvas.drawPath(path, mPaint)
    canvas.drawPath(path, mDashPaint)
  }

  override fun onMeasure() {
    mContract?.let {
      mLineSegmentSize = it.getTotalLineLength() / PATH_DASH_SEGMENTS
    }
    reset()
  }

  private fun getShader(color1: Int, color2: Int): LinearGradient {
    return LinearGradient(
        0F,
        0F,
        END_GRADIENT,
        END_GRADIENT,
        color1,
        color2,
        Shader.TileMode.MIRROR)
  }

  private fun isPending(): Boolean {
    val indeterminateAnim = mIndeterminateAnimation ?: return false
    return indeterminateAnim.isRunning
  }

  private fun reset() {
    if (isPending()) {
      stop()
      start()
    }
  }
}