package com.familypedia.utils.imagepicker.helper

import androidx.core.content.FileProvider
import com.familypedia.utils.imagepicker.features.DefaultImagePickerComponents
import com.familypedia.utils.imagepicker.features.ImagePickerComponentsHolder

class ImagePickerFileProvider : FileProvider() {
    override fun onCreate(): Boolean {
        ImagePickerComponentsHolder.setInternalComponent(DefaultImagePickerComponents(context!!))
        return super.onCreate()
    }
}