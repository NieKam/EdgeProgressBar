package com.niekam.sample.ui

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import com.niekam.edgeprogressbar.indeterminate.EffectType
import com.niekam.sample.R
import kotlinx.android.synthetic.main.activity_main.*
import org.xdty.preference.colorpicker.ColorPickerDialog

class MainActivity : AppCompatActivity(), MainPresenter.ViewContract {

    private val mPresenter = MainPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        initViews()
        initValues()
        initListeners()
    }

    private fun initViews() {
        main_animationSwitch.setOnCheckedChangeListener(mAnimationSwitchListener)
    }

    private fun initSpinner() {
        val spinnerArray = EffectType.values().map { it.name }
        val adapter: ArrayAdapter<String> =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerArray)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        main_effectTypeSpinner.adapter = adapter
    }

    private fun initValues() {
        val progress = main_edgeProgressView.getProgress()
        main_progressSeekBar.progress = progress.toInt()
        main_progressTextView.text = "$progress"
        main_indeterminateSwitch.isChecked = main_edgeProgressView.isIndeterminate()
        main_strokeWidthSeekbar.progress = main_edgeProgressView.getLineWidth()
        initSpinner()

        if (main_edgeProgressView.isIndeterminate()) {
            main_effectTypeSpinner.setSelection(main_edgeProgressView.getEffectType()!!.ordinal)
        }
    }

    private fun initListeners() {
        main_indeterminateSwitch.setOnCheckedChangeListener(mIndeterminateSwitchListener)

        main_strokeWidthSeekbar.setOnSeekBarChangeListener(mSeekBarChangeListener)
        main_cornerRadiusSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener)
        main_progressSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener)

        main_primaryColorTextView.setOnClickListener {
            mPresenter.onPrimaryColorClicked(
                main_primaryColorTextView.background as ColorDrawable
            )
        }
        main_secondaryColorTextView.setOnClickListener {
            mPresenter.onSecondaryColorClicked(
                main_secondaryColorTextView.background as ColorDrawable
            )
        }

        main_effectTypeSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                main_edgeProgressView.setEffect(EffectType.values()[position])
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
        return main_edgeProgressView.isIndeterminate()
    }

    override fun getProgressValue(): Int = main_progressSeekBar.progress

    override fun setProgressText(progress: String) {
        main_progressTextView.text = progress
    }

    override fun setMaxProgress(max: Int) {
        main_edgeProgressView.setMax(max)
        main_progressSeekBar.max = max
    }

    override fun setEdgeProgress(progress: Int, withAnimation: Boolean) {
        main_edgeProgressView.setProgress(progress.toFloat(), withAnimation)
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
            TransitionManager.beginDelayedTransition(main_rootLayout)
        }

        ConstraintSet().apply {
            clone(main_rootLayout)
            setVisibility(R.id.main_sectionProgress, visibility)
            setVisibility(R.id.main_progressTextView, visibility)
            setVisibility(R.id.main_progressSeekBar, visibility)
            setVisibility(R.id.main_animationSwitch, visibility)
            setVisibility(R.id.main_effectTypeSpinner, spinnerVisibility)
            applyTo(main_rootLayout)
        }
    }

    override fun setIndeterminate(isIndeterminate: Boolean) {
        main_edgeProgressView.setIndeterminate(isIndeterminate)
    }

    override fun setPrimaryColor(color: Int) {
        main_primaryColorTextView.setBackgroundColor(color)
        main_edgeProgressView.setFirstColor(color)
    }

    override fun setSecondaryColor(color: Int) {
        main_secondaryColorTextView.setBackgroundColor(color)
        main_edgeProgressView.setSecondColor(color)
    }

    override fun showDialog(dialog: ColorPickerDialog) {
        dialog.show(fragmentManager, "color_dialog")
    }

    private val mAnimationSwitchListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        mPresenter.onAnimationSwitchChecked(isChecked)
    }

    private val mIndeterminateSwitchListener =
        CompoundButton.OnCheckedChangeListener { _, isChecked ->
            mPresenter.onIndeterminateSwitchChecked(isChecked)
        }

    private val mSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            when (seekBar.id) {
                R.id.main_progressSeekBar -> mPresenter.onProgressChanged(progress)
                R.id.main_cornerRadiusSeekBar -> main_edgeProgressView.setCornerRadius(progress)
                R.id.main_strokeWidthSeekbar -> main_edgeProgressView.setLineWidth(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }
}
