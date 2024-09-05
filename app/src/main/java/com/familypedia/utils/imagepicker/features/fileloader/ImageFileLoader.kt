package com.familypedia.utils.imagepicker.features.fileloader

import com.familypedia.utils.imagepicker.features.ImagePickerConfig
import com.familypedia.utils.imagepicker.features.common.ImageLoaderListener

interface ImageFileLoader {
    fun loadDeviceImages(config: ImagePickerConfig, listener: ImageLoaderListener)
    fun abortLoadImages()
}