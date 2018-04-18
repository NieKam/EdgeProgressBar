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
  }

  companion object Constants {
    private val STEP_WITH_ANIMATIONS = 10
    private val MAX = 100
  }

  private var mIsAnimationEnabled = false

  override fun onViewAttached() {
    super.onViewAttached()
    require(view != null) { "View cannot be null" }

    val viewContract = view as ViewContract
    viewContract.setProgressText(viewContract.getProgressValue().toString())
    viewContract.setMaxProgress(MAX)
  }

  fun onProgressChanged(progress: Int) {
    if (mIsAnimationEnabled && progress % STEP_WITH_ANIMATIONS != 0) {
      return
    }

    view?.setProgressText(progress.toString())
    view?.setEdgeProgress(progress, mIsAnimationEnabled)
  }

  fun onIndeterminateSwitchChecked(isChecked: Boolean) {
    view?.setProgressSectionVisible(!isChecked)
    view?.setIndeterminate(isChecked)
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