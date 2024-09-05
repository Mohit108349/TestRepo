package com.familypedia.utils.imagepicker.features.common

import com.familypedia.utils.imagepicker.features.ImagePickerSavePath
import com.familypedia.utils.imagepicker.features.ReturnMode

abstract class BaseConfig {
    abstract var savePath: ImagePickerSavePath
    abstract var returnMode: ReturnMode
    abstract var isSaveImage: Boolean
}