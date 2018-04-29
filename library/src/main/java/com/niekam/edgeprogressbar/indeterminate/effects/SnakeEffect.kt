package com.niekam.edgeprogressbar.indeterminate.effects

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import com.niekam.edgeprogressbar.Constants
import com.niekam.edgeprogressbar.indeterminate.Effect
import com.niekam.edgeprogressbar.indeterminate.EffectContract
import com.niekam.edgeprogressbar.initLinePaint


/**
 * Copyright by Kamil Niezrecki
 */
class SnakeEffect : Effect {
  private companion object {
    const val TAG = "Snake"
    const val PATH_DASH_SEGMENTS = 2
    const val MOVE_ANIMATION_DURATION_MS = 2400L
    const val END_GRADIENT = 200F
  }

  private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val mPath = Path()

  private var mColorPrimary: Int = Constants.DEFAULT_COLOR
  private var mContract: EffectContract? = null
  private var mMoveAnimation: ValueAnimator? = null
  private var mLineSegmentSize: Float = 0F

  override fun onAttached(contract: EffectContract) {
    mContract = contract
    mColorPrimary = contract.getColors()[0]
    mPaint.initLinePaint(contract.getLineWidth(), mColorPrimary)
    mPaint.shader = getShader(contract.getColors())
  }

  override fun onDetached() {
    stop()
    mContract = null
  }

  override fun onLineWidthChange(width: Float) {
    mPaint.strokeWidth = width
  }

  override fun onColorsChange(colors: IntArray) {
    mColorPrimary = colors[0]
    mPaint.shader = getShader(colors)
    reset()
  }

  override fun start() {
    require(mContract != null)
    val contract = mContract as EffectContract

    if (isPending()) {
      Log.e(TAG, "Move animation already running. Exit")
      return
    }

    mMoveAnimation = ObjectAnimator.ofFloat((contract.getTotalLength() * PATH_DASH_SEGMENTS), 0F)
    mMoveAnimation?.repeatCount = ValueAnimator.INFINITE
    mMoveAnimation?.duration = MOVE_ANIMATION_DURATION_MS
    mMoveAnimation?.interpolator = AccelerateDecelerateInterpolator()
    mMoveAnimation?.addUpdateListener { it ->
      mPaint.pathEffect = DashPathEffect(
          floatArrayOf(mLineSegmentSize, mLineSegmentSize),
          it.animatedValue as Float)
      mContract?.invalidate()
    }

    mMoveAnimation?.start()

    mPaint.color = mColorPrimary
  }

  override fun stop() {
    if (!isPending()) {
      Log.e(TAG, "Indeterminate animation already stopped. Exit")
      return
    }

    mMoveAnimation?.cancel()
    mMoveAnimation = null
  }

  override fun onDraw(canvas: Canvas) {
    canvas.drawPath(mPath, mPaint)
  }

  override fun onMeasure() {
    require(mContract != null)
    val contract = mContract as EffectContract

    mLineSegmentSize = contract.getTotalLength() / PATH_DASH_SEGMENTS
    mPath.reset()
    mPath.addRect(0F, 0F, contract.width(), contract.height(), Path.Direction.CW)
    reset()
  }

  private fun isPending(): Boolean {
    val anim = mMoveAnimation ?: return false
    return anim.isRunning
  }

  private fun getShader(colors: IntArray): LinearGradient {
    return LinearGradient(
        0F,
        0F,
        END_GRADIENT,
        END_GRADIENT,
        colors[0],
        colors[1],
        Shader.TileMode.MIRROR)
  }

  private fun reset() {
    if (isPending()) {
      stop()
      start()
    }
  }
}