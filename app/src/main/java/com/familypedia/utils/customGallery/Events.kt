package com.familypedia.utils.customGallery

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable


data class ImageModal(
    var imageUrl: String,
    var isSelected: Boolean,
    var type: Boolean,
    var countSelect: Int,
    var imagePosition: Int,
    var iseditable: Boolean,
    var imageId: Int

) : Serializable


@SuppressLint("ParcelCreator")
data class SelectedImage(
    val imageUrl: String,
    val adapterPosition: Int,
    val isEditable: Boolean,
    val imageId: Int,
    var isSelected: Boolean?=false,
) :  Parcelable {
    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        TODO("Not yet implemented")
    }
}
