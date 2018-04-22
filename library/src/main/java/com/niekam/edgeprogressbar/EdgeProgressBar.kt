package com.niekam.edgeprogressbar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import com.niekam.edgeprogressbar.Constants.BLANK_LINE_SIZE
import com.niekam.edgeprogressbar.Constants.DEFAULT_MAX
import com.niekam.edgeprogressbar.Constants.DEFAULT_PROGRESS_DURATION_MS
import com.niekam.edgeprogressbar.Constants.DEFAULT_STEP
import com.niekam.edgeprogressbar.Constants.TINT_ALPHA
import com.niekam.edgeprogressbar.Constants.VISIBILITY_ANIM_DURATION
import com.niekam.edgeprogressbar.effects.Effect
import com.niekam.edgeprogressbar.effects.ZigZagEffect

/**
 * Copyright by Kamil Niezrecki
 */

class EdgeProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

  private val mProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val mTintPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val mProgressPath = Path()
  private val mPathMeasure = PathMeasure()

  private var mProgressPhase = BLANK_LINE_SIZE
  private var mStep = DEFAULT_STEP
  private var mProgressAnimation: ValueAnimator? = null
  private var mTotalLength: Float = 0F
  private var mWidth = -1
  private var mHeight = -1

  private val mZigZagEffect: Effect = ZigZagEffect()

  /**
   * Public
   */
  var progressAnimationDuration = DEFAULT_PROGRESS_DURATION_MS

  var progressLineColor = context.resources.getDefaultColor()
    set(value) {
      mProgressPaint.color = value
      field = value
      maybeResetIndeterminate()
      invalidate()
    }

  var tintColor = Color.TRANSPARENT
    set(value) {
      mTintPaint.color = value
      mTintPaint.alpha = TINT_ALPHA
      field = value
      maybeResetIndeterminate()
      invalidate()
    }

  var max = DEFAULT_MAX
    set(value) {
      field = value
      mStep = BLANK_LINE_SIZE / value
    }

  private var mProgress = 0F
  var progress: Float
    get() {
      return mProgress
    }
    set(value) {
      setProgress(value, false)
    }

  private var mIsIndeterminate = false
  var indeterminate: Boolean
    get() {
      return mIsIndeterminate
    }
    set(value) {
      mIsIndeterminate = value
      if (mIsIndeterminate) {
        mZigZagEffect.start()
      } else if (mZigZagEffect.isPending()) {
        mZigZagEffect.stop()
        drawProgress(mProgressPhase)
      }
    }

  private var mLineWidthSavedValue : Float = 0F
  private var mLineWidth: Float = 0F
  var lineWidth: Int
    get() {
      return mLineWidth.pxToDp
    }
    set(value) {
      mLineWidth = value.dpToPx
      mProgressPaint.strokeWidth = mLineWidth
      mTintPaint.strokeWidth = mLineWidth
      mZigZagEffect.setStrokeWidth(width)
    }

  /**
   * Init
   */
  init {
    val a = context.obtainStyledAttributes(attrs, R.styleable.EdgeProgressBar, defStyleAttr, 0)

    try {
      mIsIndeterminate = a.getBoolean(R.styleable.EdgeProgressBar_indeterminate, mIsIndeterminate)
      progressLineColor = a.getColor(
          R.styleable.EdgeProgressBar_progress_color,
          context.resources.getDefaultColor())
      tintColor = a.getColor(
          R.styleable.EdgeProgressBar_tint_color,
          Color.TRANSPARENT)
      mLineWidth = a.getDimension(
          R.styleable.EdgeProgressBar_line_width,
          context.resources.getDefaultStrokeWidth())
      max = a.getInt(
          R.styleable.EdgeProgressBar_max,
          max)
      mProgress = a.getFloat(
          R.styleable.EdgeProgressBar_start_progress,
          mProgress)
      progressAnimationDuration = a.getInt(
          R.styleable.EdgeProgressBar_progress_anim_duration,
          DEFAULT_PROGRESS_DURATION_MS)
    } finally {
      a.recycle()
    }

    mProgressPaint.initLinePaint(mLineWidth, progressLineColor)
    mTintPaint.initLinePaint(mLineWidth, tintColor)
    mTintPaint.alpha = Constants.TINT_ALPHA
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    if (mIsIndeterminate) {
      mZigZagEffect.stop()
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    if (mIsIndeterminate) {
      mZigZagEffect.start()
    }
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    val width = measuredWidth
    val height = measuredHeight

    if (width != mWidth || height != mHeight) {
      transformPath(width, height)
      mZigZagEffect.onSizeChanged(width, height)
    }
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    if (mIsIndeterminate) {
      mZigZagEffect.onDraw(canvas)
    } else {
      canvas.drawPath(mProgressPath, mTintPaint)
      canvas.drawPath(mProgressPath, mProgressPaint)
    }
  }

  /**
   * Public API
   */
  fun setProgress(progress: Float, animation: Boolean) {
    val normalizedProgress = progress.coerceIn(0F, max.toFloat())
    if (mIsIndeterminate || mProgress == normalizedProgress) {
      return
    }

    mProgress = normalizedProgress
    val targetPhase = getPhaseForProgress(mProgress)
    if (animation) {
      startProgressUpdateAnimation(targetPhase)
    } else {
      drawProgress(targetPhase)
    }
  }

  fun hide() {
    if (visibility == GONE) {
      return
    }

    // Save original value
    mLineWidthSavedValue = mLineWidth

    val animation = ObjectAnimator.ofFloat(mLineWidth, 0F)
    animation?.duration = VISIBILITY_ANIM_DURATION
    animation?.interpolator = AccelerateInterpolator()
    animation?.addUpdateListener {
      mLineWidth = it.animatedValue as Float
      mProgressPaint.strokeWidth = mLineWidth
      mTintPaint.strokeWidth = mLineWidth
      invalidate()
    }
    animation.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(animation: Animator?) {
        visibility = GONE
      }
    })
    animation?.start()

  }

  fun show() {
    if (visibility == VISIBLE) {
      return
    }

    val animation = ObjectAnimator.ofFloat(0F, mLineWidthSavedValue)
    animation?.duration = VISIBILITY_ANIM_DURATION
    animation?.interpolator = AccelerateInterpolator()
    animation?.addUpdateListener {
      mLineWidth = it.animatedValue as Float
      mProgressPaint.strokeWidth = mLineWidth
      mTintPaint.strokeWidth = mLineWidth
      invalidate()
    }
    animation.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationStart(animation: Animator?) {
        visibility = VISIBLE
      }
    })
    animation?.start()

  }

  /**
   * Private API
   */

  private fun transformPath(width: Int, height: Int) {
    mWidth = width
    mHeight = height

    mProgressPath.reset()
    mProgressPath.addRect(0F, 0F, width.toFloat(), height.toFloat(), Path.Direction.CW)

    mPathMeasure.setPath(mProgressPath, false)
    mTotalLength = mPathMeasure.length

    mProgressPaint.pathEffect = createPathEffect(mTotalLength, getPhaseForProgress(mProgress))

    if (mIsIndeterminate) {
      maybeResetIndeterminate()
    }
  }

  private fun getPhaseForProgress(progress: Float): Float = BLANK_LINE_SIZE - (progress * mStep)

  private fun drawProgress(targetPhase: Float) {
    mProgressPhase = targetPhase
    mProgressPaint.pathEffect = createPathEffect(mTotalLength, mProgressPhase)
    invalidate()
  }

  private fun maybeResetIndeterminate() {
    if (mZigZagEffect.isPending()) {
      mZigZagEffect.stop()
      mZigZagEffect.start()
    }
  }

  private fun startProgressUpdateAnimation(targetPhase: Float) {
    val backToZero = targetPhase == BLANK_LINE_SIZE && mProgressPhase == 0F

    if (backToZero) {
      // Current progress is max, next is zero. Force redraw to don't animate back progress.
      drawProgress(BLANK_LINE_SIZE)
      return
    }

    mProgressAnimation = ObjectAnimator.ofFloat(mProgressPhase, targetPhase)
    mProgressAnimation?.duration = progressAnimationDuration.toLong()
    mProgressAnimation?.interpolator = LinearInterpolator()
    mProgressAnimation?.addUpdateListener {
      mProgressPaint.pathEffect = createPathEffect(mTotalLength, it.animatedValue as Float)
      invalidate()
    }
    mProgressAnimation?.start()

    mProgressPhase = targetPhase
  }

  private fun createPathEffect(pathLength: Float, phase: Float): DashPathEffect {
    return DashPathEffect(floatArrayOf(pathLength, pathLength), (phase * pathLength))
  }
}
