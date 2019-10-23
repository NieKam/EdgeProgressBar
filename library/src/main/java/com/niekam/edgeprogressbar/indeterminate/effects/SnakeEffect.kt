package com.niekam.edgeprogressbar.indeterminate.effects

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.*
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import com.niekam.edgeprogressbar.Constants.END_GRADIENT
import com.niekam.edgeprogressbar.indeterminate.Effect
import com.niekam.edgeprogressbar.indeterminate.ViewContract
import com.niekam.edgeprogressbar.initLinePaint


/**
 * Copyright by Kamil Niezrecki
 */
class SnakeEffect : Effect {
    private companion object {
        const val TAG = "Snake"
        const val PATH_DASH_SEGMENTS = 2
        const val MOVE_ANIMATION_DURATION_MS = 3600L
    }

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mContract: ViewContract? = null
    private var mMoveAnimation: ValueAnimator? = null
    private var mLineSegmentSize: Float = 0F

    override fun onAttached(contract: ViewContract) {
        mContract = contract
        mPaint.initLinePaint(contract.getLineWidthInPx(), contract.getFirstColor())
        mPaint.shader = getShader(contract.getFirstColor(), contract.getSecondaryColor())
        startEffect()
    }

    override fun onDetached() {
        stopEffect()
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


    private fun startEffect() {
        require(mContract != null)
        val contract = mContract as ViewContract

        if (isPending()) {
            Log.e(TAG, "Move animation already running. Exit")
            return
        }

        mMoveAnimation = ObjectAnimator.ofFloat((contract.getTotalLineLength() * PATH_DASH_SEGMENTS), 0F).apply {
            repeatCount = ValueAnimator.INFINITE
            duration = MOVE_ANIMATION_DURATION_MS
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                mPaint.pathEffect = DashPathEffect(
                    floatArrayOf(mLineSegmentSize, mLineSegmentSize),
                    it.animatedValue as Float
                )
                mContract?.requestInvalidate()
            }
        }.also { it.start() }
        mPaint.color = contract.getFirstColor()
    }

    private fun stopEffect() {
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
            Shader.TileMode.MIRROR
        )
    }

    private fun reset() {
        if (isPending()) {
            stopEffect()
            startEffect()
        }
    }
}