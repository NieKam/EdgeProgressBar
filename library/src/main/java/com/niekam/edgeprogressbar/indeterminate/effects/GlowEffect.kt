package com.niekam.edgeprogressbar.indeterminate.effects

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.*
import android.util.Log
import android.view.animation.LinearInterpolator
import com.niekam.edgeprogressbar.Constants
import com.niekam.edgeprogressbar.indeterminate.Effect
import com.niekam.edgeprogressbar.indeterminate.ViewContract
import com.niekam.edgeprogressbar.initLinePaint

class GlowEffect : Effect {
    private companion object {
        const val TAG = "GlowEffect"
        const val INDETERMINATE_ANIMATION_DURATION_MS = 2100L
        const val BLUR = 12F
    }

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mContract: ViewContract? = null
    private var mLineSegmentSize: Float = 0F
    private var mIndeterminateAnimation: ValueAnimator? = null

    override fun onAttached(contract: ViewContract) {
        contract.let {
            mContract = it
            mPaint.initLinePaint(it.getLineWidthInPx(), it.getFirstColor())
            mPaint.shader = getShader(it.getFirstColor(), it.getSecondaryColor())
            mPaint.maskFilter = BlurMaskFilter(BLUR, BlurMaskFilter.Blur.SOLID)
        }

        startEffect()
    }

    override fun onDetached() {
        stopEffect()
        mContract = null
    }

    override fun onLineWidthChange(width: Float) {
        mPaint.strokeWidth = width
        reset()
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

    private fun startEffect() {
        val contract = requireNotNull(mContract)

        if (isPending()) {
            Log.e(TAG, "Indeterminate animation already running. Exit")
            return
        }
        mIndeterminateAnimation = ObjectAnimator.ofFloat(contract.getLineWidthInPx(), 0F).apply {
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            duration = INDETERMINATE_ANIMATION_DURATION_MS
            interpolator = LinearInterpolator()
            addUpdateListener { listener ->
                mPaint.strokeWidth = listener.animatedValue as Float
                mContract?.requestInvalidate()
            }
        }.also { it.start() }
    }

    private fun stopEffect() {
        if (!isPending()) {
            Log.e(TAG, "Indeterminate animation already stopped. Exit")
            return
        }
        mIndeterminateAnimation?.cancel()
        mIndeterminateAnimation = null
    }

    override fun onDraw(canvas: Canvas, path: Path) {
        canvas.drawPath(path, mPaint)
    }

    override fun onMeasure() {
        mLineSegmentSize = requireNotNull(mContract).getTotalLineLength()
        reset()
    }

    private fun getShader(color1: Int, color2: Int): LinearGradient {
        return LinearGradient(
            0F,
            0F,
            Constants.END_GRADIENT,
            Constants.END_GRADIENT,
            color1,
            color2,
            Shader.TileMode.MIRROR
        )
    }

    private fun isPending(): Boolean {
        val indeterminateAnim = mIndeterminateAnimation ?: return false
        return indeterminateAnim.isRunning
    }

    private fun reset() {
        if (isPending()) {
            stopEffect()
            startEffect()
        }
    }
}