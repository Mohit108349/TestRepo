package com.familypedia.utils.imagepicker.model

import android.content.ContentUris
import android.net.Uri
import com.familypedia.utils.imagepicker.helper.ImagePickerUtils
import com.familypedia.utils.imagepicker.model.Image

object ImageFactory {
    @JvmStatic
    fun singleImage(uri: Uri, path: String): List<Image> {
        return listOf(
            Image(
            id = ContentUris.parseId(uri),
            name = ImagePickerUtils.getNameFromFilePath(path),
            path = path
        )
        )
    }
}