package com.niekam.sample.ui

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v7.app.AppCompatActivity
import android.transition.TransitionManager
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import com.niekam.edgeprogressbar.EdgeProgress
import com.niekam.edgeprogressbar.indeterminate.EffectType
import com.niekam.sample.R
import org.xdty.preference.colorpicker.ColorPickerDialog


class MainActivity : AppCompatActivity(), MainPresenter.ViewContract {

  private val mPresenter = MainPresenter()

  private lateinit var mEdgeProgress: EdgeProgress
  private lateinit var mProgressText: TextView
  private lateinit var mProgressSeekBar: SeekBar
  private lateinit var mRootLayout: ConstraintLayout
  private lateinit var mPrimaryColorView: View
  private lateinit var mSecondaryColorView: View
  private lateinit var mIndeterminateSwitch: Switch
  private lateinit var mStrokeWidthSeekBar: SeekBar
  private lateinit var mEffectTypeSpinner: Spinner

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
    mEffectTypeSpinner = findViewById(R.id.effet_type_spinner)
    findViewById<Switch>(R.id.animation_switch).setOnCheckedChangeListener(mAnimationSwitchListener)
  }

  private fun initSpinner() {
    val spinnerArray = EffectType.values().map { it.name }
    val adapter: ArrayAdapter<String> = ArrayAdapter(
        this, android.R.layout.simple_spinner_item, spinnerArray)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    mEffectTypeSpinner.adapter = adapter

  }

  private fun initValues() {
    val progress = mEdgeProgress.getProgress()
    mProgressSeekBar.progress = progress.toInt()
    mProgressText.text = "$progress"
    mIndeterminateSwitch.isChecked = mEdgeProgress.isIndeterminate()
    mStrokeWidthSeekBar.progress = mEdgeProgress.getLineWidth()
    initSpinner()

    if (mEdgeProgress.isIndeterminate()) {
      mEffectTypeSpinner.setSelection(mEdgeProgress.getEffectType()!!.ordinal)
    }
  }

  private fun initListeners() {
    mIndeterminateSwitch.setOnCheckedChangeListener(mIndeterminateSwitchListener)
    mStrokeWidthSeekBar.setOnSeekBarChangeListener(mStrokeWidthChangeListener)
    mProgressSeekBar.setOnSeekBarChangeListener(mProgressBarChangeListener)
    mPrimaryColorView.setOnClickListener { mPresenter.onPrimaryColorClicked(mPrimaryColorView.background as ColorDrawable) }
    mSecondaryColorView.setOnClickListener { mPresenter.onSecondaryColorClicked(mPrimaryColorView.background as ColorDrawable) }
    mEffectTypeSpinner.onItemSelectedListener = object : OnItemSelectedListener {
      override fun onItemSelected(
          parentView: AdapterView<*>,
          selectedItemView: View?,
          position: Int,
          id: Long) {
        mEdgeProgress.setEffect(EffectType.values()[position])
      }

      override fun onNothingSelected(parentView: AdapterView<*>) {}
    }
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
    return mEdgeProgress.isIndeterminate()
  }

  override fun getProgressValue(): Int = mProgressSeekBar.progress

  override fun setProgressText(progress: String) {
    mProgressText.text = progress
  }

  override fun setMaxProgress(max: Int) {
    mEdgeProgress.setMax(max)
    mProgressSeekBar.max = max
  }

  override fun setEdgeProgress(progress: Int, withAnimation: Boolean) {
    mEdgeProgress.setProgress(progress.toFloat(), withAnimation)
  }

  override fun setProgressSectionVisible(isEnabled: Boolean) {
    val visibility: Int
    val spinnerVisibility: Int

    if (isEnabled) {
      spinnerVisibility = View.GONE
      visibility = View.VISIBLE
    } else {
      spinnerVisibility = View.VISIBLE
      visibility = View.GONE
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      TransitionManager.beginDelayedTransition(mRootLayout)
    }

    ConstraintSet().apply {
      clone(mRootLayout)
      setVisibility(R.id.section_title_progress, visibility)
      setVisibility(R.id.progress_text, visibility)
      setVisibility(R.id.progress_seekbar, visibility)
      setVisibility(R.id.animation_switch, visibility)
      setVisibility(R.id.effet_type_spinner, spinnerVisibility)
      applyTo(mRootLayout)
    }
  }

  override fun setIndeterminate(isIndeterminate: Boolean) {
    mEdgeProgress.setIndeterminate(isIndeterminate)
  }

  override fun setPrimaryColor(color: Int) {
    mPrimaryColorView.setBackgroundColor(color)
    mEdgeProgress.setFirstColor(color)
  }

  override fun setSecondaryColor(color: Int) {
    mSecondaryColorView.setBackgroundColor(color)
    mEdgeProgress.setSecondColor(color)
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
      mEdgeProgress.setLineWidth(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }
  }
}
