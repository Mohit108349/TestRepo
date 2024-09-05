package com.familypedia.utils.imagepicker.features.imageloader

import android.widget.ImageView
import com.familypedia.utils.imagepicker.model.Image

interface ImageLoader {
    fun loadImage(image: Image, imageView: ImageView, imageType: ImageType)
}