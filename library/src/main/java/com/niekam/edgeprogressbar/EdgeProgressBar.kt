package com.niekam.edgeprogressbar

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import com.niekam.edgeprogressbar.Constants.BLANK_LINE_SIZE
import com.niekam.edgeprogressbar.Constants.DEFAULT_INDETERMINATE_TYPE
import com.niekam.edgeprogressbar.Constants.DEFAULT_MAX
import com.niekam.edgeprogressbar.Constants.DEFAULT_PROGRESS_DURATION_MS
import com.niekam.edgeprogressbar.Constants.DEFAULT_STEP
import com.niekam.edgeprogressbar.Constants.INDETERMINATE_ANIMATION_DURATION_MS
import com.niekam.edgeprogressbar.Constants.PATH_DASH_SEGMENTS
import com.niekam.edgeprogressbar.Constants.TAG

/**
 * Copyright by Kamil Niezrecki
 */

class EdgeProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

  private val mPrimaryLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val mSecondaryLinePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
  private val mPath = Path()
  private val mPathMeasure = PathMeasure()

  private var mProgressPhase = BLANK_LINE_SIZE
  private var mIndeterminateAnimation: ValueAnimator? = null
  private var mStep = DEFAULT_STEP
  private var mProgressAnimation: ValueAnimator? = null
  private var mTotalLength: Float = 0F
  private var mLineSegmentSize: Float = 0F
  private var mWidth = -1
  private var mHeight = -1
  private var mIndeterminateType: Int

  /**
   * Public
   */
  var progressAnimationDuration = DEFAULT_PROGRESS_DURATION_MS
  
  var primaryColor = context.resources.getDefaultColor()
    set(value) {
      mPrimaryLinePaint.color = value
      field = value
      maybeResetIndeterminate()
      invalidate()
    }

  var secondaryColor = context.resources.getDefaultColor()
    set(value) {
      mSecondaryLinePaint.color = value
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
        startIndeterminateAnimations()
      } else if (isIndeterminateAnimPending()) {
        stopAnimatingIndeterminate()
        drawProgress(mProgressPhase)
      }
    }

  private var mStrokeWidth: Float = 0f
  var strokeWidth: Int
    get() {
      return mStrokeWidth.pxToDp
    }
    set(value) {
      mStrokeWidth = value.dpToPx
      mPrimaryLinePaint.strokeWidth = mStrokeWidth
      mSecondaryLinePaint.strokeWidth = mStrokeWidth
    }

  /**
   * Init
   */
  init {
    val a = context.obtainStyledAttributes(attrs, R.styleable.EdgeProgressBar, defStyleAttr, 0)

    try {
      mIndeterminateType = a.getInt(
          R.styleable.EdgeProgressBar_indeterminate_type,
          DEFAULT_INDETERMINATE_TYPE)
      mIsIndeterminate = a.getBoolean(R.styleable.EdgeProgressBar_indeterminate, mIsIndeterminate)
      primaryColor = a.getColor(
          R.styleable.EdgeProgressBar_primary_color,
          context.resources.getDefaultColor())
      secondaryColor = a.getColor(
          R.styleable.EdgeProgressBar_secondary_color,
          context.resources.getDefaultColor())
      mStrokeWidth = a.getDimension(
          R.styleable.EdgeProgressBar_stroke_width,
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

    initLinePaint(mPrimaryLinePaint, primaryColor)
    initLinePaint(mSecondaryLinePaint, secondaryColor)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    stopAnimatingIndeterminate()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    if (mIsIndeterminate) {
      startIndeterminateAnimations()
    }
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    val width = measuredWidth
    val height = measuredHeight

    if (width != mWidth || height != mHeight) {
      transformPath(width, height)
    }
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    if (mIsIndeterminate) {
      canvas.drawPath(mPath, mPrimaryLinePaint)
      canvas.drawPath(mPath, mSecondaryLinePaint)
    } else {
      canvas.drawPath(mPath, mPrimaryLinePaint)
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

  /**
   * Private API
   */
  private fun initLinePaint(line: Paint, color: Int) {
    line.color = color
    line.style = Paint.Style.STROKE
    line.strokeWidth = mStrokeWidth
    line.isDither = true
    line.style = Paint.Style.STROKE
    line.strokeJoin = Paint.Join.ROUND
    line.strokeCap = Paint.Cap.ROUND
  }

  private fun transformPath(width: Int, height: Int) {
    mWidth = width
    mHeight = height

    mPath.reset()
    mPath.addRect(0F, 0F, width.toFloat(), height.toFloat(), Path.Direction.CW)

    mPathMeasure.setPath(mPath, false)
    mTotalLength = mPathMeasure.length

    mLineSegmentSize = mTotalLength / PATH_DASH_SEGMENTS
    mPrimaryLinePaint.pathEffect = createPathEffect(mTotalLength, getPhaseForProgress(mProgress))

    if (mIsIndeterminate) {
      maybeResetIndeterminate()
    }
  }

  private fun getPhaseForProgress(progress: Float): Float = BLANK_LINE_SIZE - (progress * mStep)

  private fun drawProgress(targetPhase: Float) {
    mProgressPhase = targetPhase
    mPrimaryLinePaint.pathEffect = createPathEffect(mTotalLength, mProgressPhase)
    invalidate()
  }

  private fun maybeResetIndeterminate() {
    if (isIndeterminateAnimPending()) {
      stopAnimatingIndeterminate()
      startIndeterminateAnimations()
    }
  }

  private fun isIndeterminateAnimPending(): Boolean {
    val indeterminateAnim = mIndeterminateAnimation ?: return false
    return indeterminateAnim.isRunning
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
      mPrimaryLinePaint.pathEffect = createPathEffect(mTotalLength, it.animatedValue as Float)
      invalidate()
    }
    mProgressAnimation?.start()

    mProgressPhase = targetPhase
  }

  private fun resetStartLine() {
    mPrimaryLinePaint.color = primaryColor
  }

  private fun resetEndLine() {
    mSecondaryLinePaint.color = secondaryColor
  }

  private fun resetStartLinePathEffect() {
    mPrimaryLinePaint.pathEffect = null
  }

  private fun startIndeterminateAnimations() {
    if (isIndeterminateAnimPending()) {
      Log.e(TAG, "Indeterminate animation already running. Exit")
      return
    }

    stopProgressAnimation()
    resetStartLinePathEffect()

    mIndeterminateAnimation = ObjectAnimator.ofFloat((mTotalLength * 2), 0F)
    mIndeterminateAnimation?.repeatCount = ValueAnimator.INFINITE
    mIndeterminateAnimation?.duration = INDETERMINATE_ANIMATION_DURATION_MS
    mIndeterminateAnimation?.interpolator = LinearInterpolator()
    mIndeterminateAnimation?.addUpdateListener { it ->
      mSecondaryLinePaint.pathEffect = DashPathEffect(
          floatArrayOf(mLineSegmentSize, mLineSegmentSize),
          it.animatedValue as Float)
      invalidate()
    }
    mIndeterminateAnimation?.start()

    val argbEvaluator = ArgbEvaluator()
    val mediatePrimaryColor = argbEvaluator.evaluate(.85F, primaryColor, secondaryColor)
    val mediateSecondaryColor = argbEvaluator.evaluate(.45F, secondaryColor, primaryColor)

    mPrimaryLinePaint.color = mediatePrimaryColor as Int
    mSecondaryLinePaint.color = mediateSecondaryColor as Int
  }

  private fun stopProgressAnimation() {
    mProgressAnimation?.cancel()
    mProgressAnimation = null
  }

  private fun stopAnimatingIndeterminate() {
    if (!isIndeterminateAnimPending()) {
      Log.e(TAG, "Indeterminate animation already stopped. Exit")
      return
    }

    resetStartLine()
    resetEndLine()

    mIndeterminateAnimation?.cancel()
    mIndeterminateAnimation = null
  }

  private fun createPathEffect(pathLength: Float, phase: Float): DashPathEffect {
    return DashPathEffect(floatArrayOf(pathLength, pathLength), (phase * pathLength))
  }
}
