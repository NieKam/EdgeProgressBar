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
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import com.niekam.edgeprogressbar.Constants.BLANK_LINE_SIZE
import com.niekam.edgeprogressbar.Constants.DEFAULT_MAX
import com.niekam.edgeprogressbar.Constants.DEFAULT_PROGRESS_DURATION_MS
import com.niekam.edgeprogressbar.Constants.DEFAULT_STEP
import com.niekam.edgeprogressbar.Constants.NOT_FOUND
import com.niekam.edgeprogressbar.Constants.TINT_ALPHA
import com.niekam.edgeprogressbar.Constants.VISIBILITY_ANIM_DURATION
import com.niekam.edgeprogressbar.indeterminate.Effect
import com.niekam.edgeprogressbar.indeterminate.EffectContract
import com.niekam.edgeprogressbar.indeterminate.EffectType

/**
 * Copyright by Kamil Niezrecki
 */

class EdgeProgress @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), EffectContract, EdgeProgressApi {

  private val mPathMeasure = PathMeasure()
  private val mRect = RectF()
  private val mProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val mProgressPath = Path()
  private val mTintPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private var mScreenHeight = 0
  private var mScreenWidth = 0
  private var mAnimateProgressChange: Boolean = false
  private var mProgressPhase = BLANK_LINE_SIZE
  private var mStep = DEFAULT_STEP
  private var mTotalLength: Float = 0F
  private var mProgressAnimationDuration = DEFAULT_PROGRESS_DURATION_MS
  private var mFirstColor: Int = context.resources.getDefaultColor()
  private var mSecondaryColor: Int = Color.TRANSPARENT
  private var mMaxProgress: Int = DEFAULT_MAX
  private var mCurrentProgress = 0F
  private var mIsIndeterminate = false
  private var mLineWidthSavedValue: Float = 0F
  private var mLineWidth: Float = 0F

  private var mIndeterminateEffect: Effect? = null
  private var mEffectType: EffectType? = null
  private var mProgressAnimation: ValueAnimator? = null

  /**
   * Init
   */
  init {
    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    val a = context.obtainStyledAttributes(attrs, R.styleable.EdgeProgress, defStyleAttr, 0)

    try {
      mIsIndeterminate = a.getBoolean(R.styleable.EdgeProgress_indeterminate, mIsIndeterminate)
      mFirstColor = a.getColor(
          R.styleable.EdgeProgress_first_color,
          context.resources.getDefaultColor())
      mSecondaryColor = a.getColor(
          R.styleable.EdgeProgress_second_color,
          Color.TRANSPARENT)
      mLineWidth = a.getDimension(
          R.styleable.EdgeProgress_line_width,
          context.resources.getDefaultStrokeWidth())
      mMaxProgress = a.getInt(
          R.styleable.EdgeProgress_max,
          mMaxProgress)
      mCurrentProgress = a.getFloat(
          R.styleable.EdgeProgress_start_progress,
          mCurrentProgress)
      mProgressAnimationDuration = a.getInt(
          R.styleable.EdgeProgress_progress_anim_duration,
          DEFAULT_PROGRESS_DURATION_MS.toInt()).toLong()

      val indeterminateType = a.getInt(
          R.styleable.EdgeProgress_indeterminate_type,
          NOT_FOUND)

      if (indeterminateType != NOT_FOUND) {
        val effectType = EffectType.values()[indeterminateType]
        mEffectType = effectType
        mIndeterminateEffect = effectType.effect
      }

    } finally {
      a.recycle()
    }

    mProgressPaint.initLinePaint(mLineWidth, mFirstColor)
    mTintPaint.initLinePaint(mLineWidth, mSecondaryColor)
    mTintPaint.alpha = Constants.TINT_ALPHA
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    mIndeterminateEffect?.onAttached(this)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    mIndeterminateEffect?.onDetached()
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    val width = measuredWidth
    val height = measuredHeight

    if (width != mScreenWidth || height != mScreenHeight) {
      mTotalLength = transformPath(width, height)
      mIndeterminateEffect?.onMeasure()
    }
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    if (mIsIndeterminate) {
      mIndeterminateEffect?.onDraw(canvas, mProgressPath)
    } else {
      canvas.drawPath(mProgressPath, mTintPaint)
      canvas.drawPath(mProgressPath, mProgressPaint)
    }
  }

  /**
   * EdgeProgress
   */

  override fun setFirstColor(color: Int) {
    mFirstColor = color
    mProgressPaint.color = mFirstColor
    mIndeterminateEffect?.onPrimaryColorChange(mFirstColor)
    invalidate()
  }

  override fun setSecondColor(color: Int) {
    mSecondaryColor = color

    mTintPaint.color = mSecondaryColor
    mTintPaint.alpha = TINT_ALPHA
    mIndeterminateEffect?.onSecondaryColorChange(mSecondaryColor)
    invalidate()
  }

  override fun setLineWidth(width: Int) {
    mLineWidth = width.dpToPx

    mProgressPaint.strokeWidth = mLineWidth
    mTintPaint.strokeWidth = mLineWidth
    mIndeterminateEffect?.onLineWidthChange(mLineWidth)
    transformPath(mScreenWidth, mScreenHeight)
    invalidate()
  }

  override fun getLineWidth(): Int {
    return mLineWidth.pxToDp
  }

  override fun setMax(max: Int) {
    mMaxProgress = max
    mStep = BLANK_LINE_SIZE / max
  }

  override fun getProgress(): Float {
    return mCurrentProgress
  }

  override fun setProgress(progress: Float, animate: Boolean) {
    mAnimateProgressChange = animate
    val normalizedProgress = progress.coerceIn(0F, mMaxProgress.toFloat())
    if (mIsIndeterminate || mCurrentProgress == normalizedProgress) {
      return
    }
    mCurrentProgress = normalizedProgress
    val targetPhase = getPhaseForProgress(mCurrentProgress)

    if (animate) {
      startProgressUpdateAnimation(targetPhase)
    } else {
      drawProgress(targetPhase)
    }
  }

  override fun setProgressAnimationDuration(duration: Long) {
    mProgressAnimationDuration = duration
  }

  override fun setIndeterminate(isIndeterminate: Boolean) {
    mIsIndeterminate = isIndeterminate

    requireNotNull(mIndeterminateEffect) { "Effect not set. Please set indeterminate effect before calling this method" }

    if (mIsIndeterminate) {
      mIndeterminateEffect?.onAttached(this)
    } else {
      mIndeterminateEffect?.onDetached()
      drawProgress(mProgressPhase)
    }
  }

  override fun isIndeterminate(): Boolean {
    return mIsIndeterminate
  }

  override fun setEffect(effect: EffectType) {
    mIndeterminateEffect?.onDetached()

    mEffectType = effect
    mIndeterminateEffect = effect.effect

    if (mIsIndeterminate) {
      mIndeterminateEffect?.onAttached(this)
      mIndeterminateEffect?.onMeasure()
    }
  }

  override fun getEffectType(): EffectType? {
    return mEffectType
  }

  override fun hide() {
    if (visibility == GONE) {
      return
    }

    // Save original value
    mLineWidthSavedValue = mLineWidth

    ObjectAnimator.ofFloat(mLineWidth, 0F).let {
      it.duration = VISIBILITY_ANIM_DURATION
      it.interpolator = AccelerateInterpolator()
      it.addUpdateListener { l ->
        mLineWidth = l.animatedValue as Float
        mProgressPaint.strokeWidth = mLineWidth
        mTintPaint.strokeWidth = mLineWidth
        invalidate()
      }
      it.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
          visibility = GONE
        }
      })
      it.start()
    }
  }

  override fun show() {
    if (visibility == VISIBLE) {
      return
    }

    ObjectAnimator.ofFloat(0F, mLineWidthSavedValue).let {
      it.duration = VISIBILITY_ANIM_DURATION
      it.interpolator = AccelerateInterpolator()
      it.addUpdateListener { l ->
        mLineWidth = l.animatedValue as Float
        mProgressPaint.strokeWidth = mLineWidth
        mTintPaint.strokeWidth = mLineWidth
        invalidate()
      }
      it.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator?) {
          visibility = VISIBLE
        }
      })
      it.start()
    }
  }

  override fun getLineWidthInPx(): Float {
    return mLineWidth
  }

  override fun getFirstColor(): Int {
    return mFirstColor
  }

  override fun getSecondaryColor(): Int {
    return mSecondaryColor
  }

  override fun getTotalLineLength(): Float {
    return mTotalLength
  }

  override fun requestInvalidate() {
    invalidate()
  }

  override fun width(): Float {
    return mScreenWidth.toFloat()
  }

  override fun height(): Float {
    return mScreenHeight.toFloat()
  }

  /**
   * Private API
   */

  private fun transformPath(width: Int, height: Int): Float {
    mScreenWidth = width
    mScreenHeight = height

    mProgressPath.reset()
    mRect.set(0F, 0F, width.toFloat(), height.toFloat())

    val inset = adjustInset()
    mRect.inset(inset, inset)
    mProgressPath.addRect(mRect, Path.Direction.CW)

    mPathMeasure.setPath(mProgressPath, false)
    mProgressPaint.pathEffect = createPathEffect(mTotalLength, getPhaseForProgress(mCurrentProgress))

    return mPathMeasure.length
  }

  private fun adjustInset(): Float {
    val maxWidth = 4.dpToPx
    return if (mLineWidth >= maxWidth) {
      mLineWidth / 3
    } else {
      0F
    }
  }

  private fun getPhaseForProgress(progress: Float): Float = BLANK_LINE_SIZE - (progress * mStep)

  private fun drawProgress(targetPhase: Float) {
    mProgressPhase = targetPhase
    mProgressPaint.pathEffect = createPathEffect(mTotalLength, mProgressPhase)
    invalidate()
  }

  private fun startProgressUpdateAnimation(targetPhase: Float) {
    val backToZero = targetPhase == BLANK_LINE_SIZE && mProgressPhase == 0F

    if (backToZero) {
      // Current progress is max, next is zero. Force redraw to don't animate back progress.
      drawProgress(BLANK_LINE_SIZE)
      return
    }

    mProgressAnimation = ObjectAnimator.ofFloat(mProgressPhase, targetPhase)
    mProgressAnimation?.let {
      it.duration = mProgressAnimationDuration
      it.interpolator = LinearInterpolator()
      it.addUpdateListener { l ->
        mProgressPaint.pathEffect = createPathEffect(mTotalLength, l.animatedValue as Float)
        invalidate()
      }
      it.start()
    }
    mProgressPhase = targetPhase
  }

  private fun createPathEffect(pathLength: Float, phase: Float): DashPathEffect {
    return DashPathEffect(floatArrayOf(pathLength, pathLength), (phase * pathLength))
  }
}
