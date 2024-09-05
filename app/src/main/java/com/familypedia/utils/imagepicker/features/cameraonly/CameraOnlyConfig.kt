package com.familypedia.utils.imagepicker.features.cameraonly

import android.os.Parcelable
import com.familypedia.utils.imagepicker.features.ImagePickerSavePath
import com.familypedia.utils.imagepicker.features.ReturnMode
import com.familypedia.utils.imagepicker.features.common.BaseConfig
import kotlinx.android.parcel.Parcelize

@Parcelize
class CameraOnlyConfig(
    override var savePath: ImagePickerSavePath = ImagePickerSavePath.DEFAULT,
    override var returnMode: ReturnMode = ReturnMode.ALL,
    override var isSaveImage: Boolean = true
) : BaseConfig(), Parcelable