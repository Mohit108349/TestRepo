package com.familypedia.utils.imagepicker.others

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.esafirm.sample.GrayscaleTransformation
import com.familypedia.utils.imagepicker.features.imageloader.ImageLoader
import com.familypedia.utils.imagepicker.features.imageloader.ImageType
import com.familypedia.utils.imagepicker.model.Image

class GrayscaleImageLoader : ImageLoader {
    override fun loadImage(image: Image, imageView: ImageView, imageType: ImageType) {
        Glide.with(imageView)
            .asBitmap()
            .load(image.path)
            .transition(BitmapTransitionOptions.withCrossFade())
            .apply(REQUEST_OPTIONS)
            .into(imageView)
    }

    companion object {
        private val REQUEST_OPTIONS = RequestOptions().transform(GrayscaleTransformation())
    }
}