package com.familypedia.utils.imagepicker.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.Interpolator
import android.widget.RelativeLayout
import androidx.annotation.StringRes
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import com.familypedia.R
import com.familypedia.databinding.EfImagepikcerSnackbarBinding

class SnackBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {

    private val binding: EfImagepikcerSnackbarBinding =
        EfImagepikcerSnackbarBinding.inflate(LayoutInflater.from(context), this)

    init {
        if (!isInEditMode) {
            val height = context.resources.getDimensionPixelSize(R.dimen.ef_height_snackbar)
            translationY = height.toFloat()
            alpha = 0f
        }
    }

    fun show(@StringRes textResId: Int, onClickListener: OnClickListener) {
        binding.efSnackbarTxtBottomCaption.text = context.getString(textResId)
        binding.efSnackbarBtnAction.setOnClickListener(onClickListener)

        animate().translationY(0f)
            .setDuration(ANIM_DURATION.toLong())
            .setInterpolator(INTERPOLATOR)
            .alpha(1f)
    }

    companion object {
        private const val ANIM_DURATION = 200
        private val INTERPOLATOR: Interpolator = FastOutLinearInInterpolator()
    }
}
