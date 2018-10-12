package com.niekam.edgeprogressbar.indeterminate.effects

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import com.niekam.edgeprogressbar.Constants.END_GRADIENT
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
  }

  private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private var mContract: EffectContract? = null
  private var mMoveAnimation: ValueAnimator? = null
  private var mLineSegmentSize: Float = 0F

  override fun onAttached(contract: EffectContract) {
    mContract = contract
    mPaint.initLinePaint(contract.getLineWidthInPx(), contract.getFirstColor())
    mPaint.shader = getShader(contract.getFirstColor(), contract.getSecondaryColor())
    start()
  }

  override fun onDetached() {
    stop()
    mContract = null
  }

  override fun onLineWidthChange(width: Float) {
    mPaint.strokeWidth = width
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
      Log.e(TAG, "Move animation already running. Exit")
      return
    }

    mMoveAnimation = ObjectAnimator.ofFloat((contract.getTotalLineLength() * PATH_DASH_SEGMENTS), 0F)
    mMoveAnimation?.repeatCount = ValueAnimator.INFINITE
    mMoveAnimation?.duration = MOVE_ANIMATION_DURATION_MS
    mMoveAnimation?.interpolator = AccelerateDecelerateInterpolator()
    mMoveAnimation?.addUpdateListener { it ->
      mPaint.pathEffect = DashPathEffect(
          floatArrayOf(mLineSegmentSize, mLineSegmentSize),
          it.animatedValue as Float)
      mContract?.requestInvalidate()
    }

    mMoveAnimation?.start()

    mPaint.color = contract.getFirstColor()
  }

  private fun stop() {
    if (!isPending()) {
      Log.e(TAG, "Indeterminate animation already stopped. Exit")
      return
    }

    mMoveAnimation?.cancel()
    mMoveAnimation = null
  }

  override fun onDraw(canvas: Canvas, path: Path) {
    canvas.drawPath(path, mPaint)
  }

  override fun onMeasure() {
    mContract?.let {
      mLineSegmentSize = it.getTotalLineLength() / PATH_DASH_SEGMENTS
    }
    reset()
  }

  private fun isPending(): Boolean {
    val anim = mMoveAnimation ?: return false
    return anim.isRunning
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

  private fun reset() {
    if (isPending()) {
      stop()
      start()
    }
  }
}