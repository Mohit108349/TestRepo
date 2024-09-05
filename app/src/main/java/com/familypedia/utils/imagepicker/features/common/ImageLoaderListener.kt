package com.familypedia.utils.imagepicker.features.common

import com.familypedia.utils.imagepicker.model.Folder
import com.familypedia.utils.imagepicker.model.Image

interface ImageLoaderListener {
    fun onImageLoaded(images: List<Image>, folders: List<Folder>)
    fun onFailed(throwable: Throwable)
}