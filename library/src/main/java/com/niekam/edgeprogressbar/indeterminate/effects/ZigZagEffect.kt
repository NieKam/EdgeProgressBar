package com.niekam.edgeprogressbar.indeterminate.effects

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.*
import android.util.Log
import android.view.animation.LinearInterpolator
import com.niekam.edgeprogressbar.Constants.END_GRADIENT
import com.niekam.edgeprogressbar.indeterminate.Effect
import com.niekam.edgeprogressbar.indeterminate.ViewContract
import com.niekam.edgeprogressbar.initLinePaint

/**
 * Copyright by Kamil Niezrecki
 */
class ZigZagEffect : Effect {
    private companion object {
        const val TAG = "ZigZag"
        const val PATH_DASH_SEGMENTS = 6
        const val DASH_LINE_COUNT = 2
        const val INDETERMINATE_ANIMATION_DURATION_MS = 5000L
    }

    private val mDashPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mContract: ViewContract? = null
    private var mIndeterminateAnimation: ValueAnimator? = null
    private var mLineSegmentSize: Float = 0F

    override fun onAttached(contract: ViewContract) {
        contract.let {
            mContract = it
            mPaint.initLinePaint(it.getLineWidthInPx(), it.getFirstColor())
            mDashPaint.initLinePaint(it.getLineWidthInPx(), it.getSecondaryColor())
            mPaint.shader = getShader(it.getFirstColor(), it.getSecondaryColor())
        }

        startEffect()
    }

    override fun onDetached() {
        stopEffect()
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

    private fun startEffect() {
        require(mContract != null)
        val contract = mContract as ViewContract

        if (isPending()) {
            Log.e(TAG, "Indeterminate animation already running. Exit")
            return
        }

        mIndeterminateAnimation = ObjectAnimator.ofFloat((contract.getTotalLineLength() * DASH_LINE_COUNT), 0F).apply {
            repeatCount = ValueAnimator.INFINITE
            duration = INDETERMINATE_ANIMATION_DURATION_MS
            interpolator = LinearInterpolator()
            addUpdateListener {
                mDashPaint.pathEffect = DashPathEffect(
                    floatArrayOf(mLineSegmentSize, mLineSegmentSize),
                    it.animatedValue as Float
                )
                mContract?.requestInvalidate()
            }
        }.also { it.start() }

        val argbEvaluator = ArgbEvaluator()
        mDashPaint.color = argbEvaluator.evaluate(
            .55F,
            contract.getFirstColor(),
            contract.getSecondaryColor()
        ) as Int
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
        canvas.run {
            drawPath(path, mPaint)
            drawPath(path, mDashPaint)
        }
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