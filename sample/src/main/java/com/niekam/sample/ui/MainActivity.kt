package com.niekam.sample.ui

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v7.app.AppCompatActivity
import android.transition.TransitionManager
import android.view.View
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import com.niekam.edgeprogressbar.EdgeProgressBar
import com.niekam.sample.R
import org.xdty.preference.colorpicker.ColorPickerDialog

class MainActivity : AppCompatActivity(), MainPresenter.ViewContract {

  private val mPresenter = MainPresenter()

  private lateinit var mEdgeProgress: EdgeProgressBar
  private lateinit var mProgressText: TextView
  private lateinit var mProgressSeekBar: SeekBar
  private lateinit var mRootLayout: ConstraintLayout
  private lateinit var mPrimaryColorView: View
  private lateinit var mSecondaryColorView: View
  private lateinit var mIndeterminateSwitch: Switch
  private lateinit var mStrokeWidthSeekBar: SeekBar

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    initViews()
    initValues()
    initListeners()
  }

  private fun initViews() {
    mStrokeWidthSeekBar = findViewById(R.id.stroke_width_seekbar)
    mIndeterminateSwitch = findViewById(R.id.indeterminate_switch)
    mProgressSeekBar = findViewById(R.id.progress_seekbar)
    mProgressText = findViewById(R.id.progress_text)
    mEdgeProgress = findViewById(R.id.edgeProgress)
    mRootLayout = findViewById(R.id.root_layout)
    mPrimaryColorView = findViewById(R.id.color_primary)
    mSecondaryColorView = findViewById(R.id.color_secondary)
    findViewById<Switch>(R.id.animation_switch).setOnCheckedChangeListener(mAnimationSwitchListener)
  }

  private fun initValues() {
    val progress = mEdgeProgress.progress
    mProgressSeekBar.progress = progress.toInt()
    mProgressText.text = "$progress"
    mIndeterminateSwitch.isChecked = mEdgeProgress.indeterminate
    mStrokeWidthSeekBar.progress = mEdgeProgress.lineWidth
  }

  private fun initListeners() {
    mIndeterminateSwitch.setOnCheckedChangeListener(mIndeterminateSwitchListener)
    mStrokeWidthSeekBar.setOnSeekBarChangeListener(mStrokeWidthChangeListener)
    mProgressSeekBar.setOnSeekBarChangeListener(mProgressBarChangeListener)
    mPrimaryColorView.setOnClickListener { mPresenter.onPrimaryColorClicked(mPrimaryColorView.background as ColorDrawable) }
    mSecondaryColorView.setOnClickListener { mPresenter.onSecondaryColorClicked(mPrimaryColorView.background as ColorDrawable) }
  }

  override fun onStart() {
    super.onStart()
    mPresenter.attachView(this)
  }

  override fun onStop() {
    mPresenter.detachView()
    super.onStop()
  }

  override fun isIndeterminate(): Boolean {
    return mEdgeProgress.indeterminate
  }

  override fun getProgressValue(): Int = mProgressSeekBar.progress

  override fun setProgressText(progress: String) {
    mProgressText.text = progress
  }

  override fun setMaxProgress(max: Int) {
    mEdgeProgress.max = max
    mProgressSeekBar.max = max
  }

  override fun setEdgeProgress(progress: Int, withAnimation: Boolean) {
    // I could call mEdgeProgress.setProgress(progress.toFloat(), withAnimation)
    // but I want to show how you can set progress in kotlin.
    if (withAnimation) {
      mEdgeProgress.setProgress(progress.toFloat(), true)
    } else {
      mEdgeProgress.progress = progress.toFloat()
    }
  }

  override fun setProgressSectionVisible(isEnabled: Boolean) {
    val visibility = if (isEnabled) View.VISIBLE else View.GONE
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      TransitionManager.beginDelayedTransition(mRootLayout)
    }

    val set = ConstraintSet()
    set.clone(mRootLayout)
    set.setVisibility(R.id.section_title_progress, visibility)
    set.setVisibility(R.id.progress_text, visibility)
    set.setVisibility(R.id.progress_seekbar, visibility)
    set.setVisibility(R.id.animation_switch, visibility)
    set.applyTo(mRootLayout)
  }

  override fun setIndeterminate(isIndeterminate: Boolean) {
    mEdgeProgress.indeterminate = isIndeterminate
  }

  override fun setPrimaryColor(color: Int) {
    mPrimaryColorView.setBackgroundColor(color)
    mEdgeProgress.progressLineColor = color
  }

  override fun setSecondaryColor(color: Int) {
    mSecondaryColorView.setBackgroundColor(color)
    mEdgeProgress.tintColor = color
  }

  override fun showDialog(dialog: ColorPickerDialog) {
    dialog.show(fragmentManager, "color_dialog")
  }

  private val mAnimationSwitchListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
    mPresenter.onAnimationSwitchChecked(isChecked)
  }

  private val mIndeterminateSwitchListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
    mPresenter.onIndeterminateSwitchChecked(isChecked)
  }

  private val mProgressBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
      mPresenter.onProgressChanged(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
  }

  private val mStrokeWidthChangeListener = object : SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
      mEdgeProgress.lineWidth = progress
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }
  }
}
