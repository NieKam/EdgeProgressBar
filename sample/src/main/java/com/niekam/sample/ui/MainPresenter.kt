package com.niekam.sample.ui

import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import com.niekam.sample.R
import com.niekam.sample.base.BaseViewPresenter
import org.xdty.preference.colorpicker.ColorPickerDialog


class MainPresenter : BaseViewPresenter<MainPresenter.ViewContract>() {
  interface ViewContract {
    fun getProgressValue(): Int
    fun setProgressText(progress: String)
    fun setEdgeProgress(progress: Int, withAnimation: Boolean)
    fun setProgressSectionVisible(isEnabled: Boolean)
    fun setMaxProgress(max: Int)
    fun setIndeterminate(isIndeterminate: Boolean)
    fun setPrimaryColor(color: Int)
    fun setSecondaryColor(color: Int)
    fun showDialog(dialog: ColorPickerDialog)
    fun getResources(): Resources
    fun isIndeterminate(): Boolean
  }

  companion object Constants {
    private const val STEP_WITH_ANIMATIONS = 5
    private const val MAX = 100
  }

  private var mIsAnimationEnabled = false
  private var mProgress = 0

  override fun onViewAttached() {
    super.onViewAttached()
    require(view != null) { "View cannot be null" }

    view?.let {
      it.setProgressText(it.getProgressValue().toString())
      it.setMaxProgress(MAX)
      it.setProgressSectionVisible(!it.isIndeterminate())
    }
  }

  fun onProgressChanged(progress: Int) {
    if (mIsAnimationEnabled && Math.abs(mProgress - progress) < STEP_WITH_ANIMATIONS) {
      return
    }

    mProgress = progress
    view?.let {
      it.setProgressText(progress.toString())
      it.setEdgeProgress(progress, mIsAnimationEnabled)
    }
  }

  fun onIndeterminateSwitchChecked(isChecked: Boolean) {
    view?.let {
      it.setProgressSectionVisible(!isChecked)
      it.setIndeterminate(isChecked)
    }
  }

  fun onAnimationSwitchChecked(isChecked: Boolean) {
    mIsAnimationEnabled = isChecked
  }

  fun onPrimaryColorClicked(color: ColorDrawable) {
    val dialog = getColorPickerDialog(color.color)
    dialog.setOnColorSelectedListener {
      view?.setPrimaryColor(it)
    }

    view?.showDialog(dialog)
  }

  fun onSecondaryColorClicked(color: ColorDrawable) {
    val dialog = getColorPickerDialog(color.color)
    dialog.setOnColorSelectedListener {
      view?.setSecondaryColor(it)
    }

    view?.showDialog(dialog)
  }

  private fun getColorPickerDialog(color: Int): ColorPickerDialog {
    return ColorPickerDialog.newInstance(
        R.string.choose_colors,
        view?.getResources()?.getIntArray(R.array.rainbow),
        color,
        5, ColorPickerDialog.SIZE_SMALL, true
    )
  }
}