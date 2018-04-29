package com.niekam.edgeprogressbar.indeterminate.effects

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import android.view.animation.LinearInterpolator
import com.niekam.edgeprogressbar.indeterminate.Effect
import com.niekam.edgeprogressbar.indeterminate.EffectContract
import com.niekam.edgeprogressbar.initLinePaint

class DashEffect : Effect {
  private companion object {
    const val TAG = "Dash"
    const val PATH_DASH_SEGMENTS = 24
    const val DASH_LINE_COUNT = 2
    const val INDETERMINATE_ANIMATION_DURATION_MS = 7_000L
  }

  private val mDashPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val mPath = Path()

  private var mColorPrimary: Int = Color.RED
  private var mColorSecondary: Int = Color.BLUE
  private var mContract: EffectContract? = null
  private var mIndeterminateAnimation: ValueAnimator? = null
  private var mLineSegmentSize: Float = 0F

  override fun onAttached(contract: EffectContract) {
    mContract = contract

    setColors(contract.getColors())
    mPaint.initLinePaint(contract.getLineWidth(), mColorPrimary)
    mDashPaint.initLinePaint(contract.getLineWidth(), mColorSecondary)
  }

  override fun onDetached() {
    stop()
    mContract = null
  }

  override fun onLineWidthChange(width: Float) {
    mPaint.strokeWidth = width
    mDashPaint.strokeWidth = width
  }

  override fun onColorsChange(colors: IntArray) {
    setColors(colors)
    reset()
  }

  private fun setColors(colors: IntArray) {
    if (colors.size == 2) {
      mColorPrimary = colors[0]
      mColorSecondary = colors[1]
    }
  }

  override fun start() {
    require(mContract != null)
    val contract = mContract as EffectContract

    if (isPending()) {
      Log.e(TAG, "Indeterminate animation already running. Exit")
      return
    }

    mIndeterminateAnimation = ObjectAnimator.ofFloat(
        (contract.getTotalLength() * DASH_LINE_COUNT), 0F)
    mIndeterminateAnimation?.repeatCount = ValueAnimator.INFINITE
    mIndeterminateAnimation?.duration = INDETERMINATE_ANIMATION_DURATION_MS
    mIndeterminateAnimation?.interpolator = LinearInterpolator()
    mIndeterminateAnimation?.addUpdateListener { it ->
      mDashPaint.pathEffect = DashPathEffect(
          floatArrayOf(mLineSegmentSize, mLineSegmentSize),
          it.animatedValue as Float)
      mContract?.invalidate()
    }
    mIndeterminateAnimation?.start()

    mPaint.color = mColorPrimary
    mDashPaint.color = mColorSecondary
  }

  override fun stop() {
    if (!isPending()) {
      Log.e(TAG, "Indeterminate animation already stopped. Exit")
      return
    }

    mIndeterminateAnimation?.cancel()
    mIndeterminateAnimation = null
  }

  override fun onDraw(canvas: Canvas) {
    canvas.drawPath(mPath, mPaint)
    canvas.drawPath(mPath, mDashPaint)
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