package com.familypedia.utils.imagepicker.others

import android.content.Context
import com.familypedia.utils.imagepicker.features.DefaultImagePickerComponents
import com.familypedia.utils.imagepicker.features.imageloader.DefaultImageLoader
import com.familypedia.utils.imagepicker.features.imageloader.ImageLoader

class CustomImagePickerComponents(
    context: Context,
    private val useCustomImageLoader: Boolean
) : DefaultImagePickerComponents(context.applicationContext) {
    override val imageLoader: ImageLoader
        get() = if (useCustomImageLoader) {
            GrayscaleImageLoader()
        } else {
            DefaultImageLoader()
        }
}