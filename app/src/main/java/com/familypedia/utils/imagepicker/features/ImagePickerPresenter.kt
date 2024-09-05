package com.familypedia.utils.imagepicker.features

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.familypedia.R
import com.familypedia.utils.imagepicker.features.camera.CameraModule
import com.familypedia.utils.imagepicker.features.common.BaseConfig
import com.familypedia.utils.imagepicker.features.common.ImageLoaderListener
import com.familypedia.utils.imagepicker.features.fileloader.DefaultImageFileLoader
import com.familypedia.utils.imagepicker.helper.ConfigUtils
import com.familypedia.utils.imagepicker.helper.state.LiveDataObservableState
import com.familypedia.utils.imagepicker.helper.state.ObservableState
import com.familypedia.utils.imagepicker.helper.state.asSingleEvent
import com.familypedia.utils.imagepicker.model.Folder
import com.familypedia.utils.imagepicker.model.Image
import java.io.File

internal class ImagePickerPresenter(
    private val imageLoader: DefaultImageFileLoader
) : ImagePickerAction {

    private val cameraModule: CameraModule = ImagePickerComponentsHolder.cameraModule

    private val stateObs = LiveDataObservableState(
        ImagePickerState(isLoading = true),
        usePostValue = true
    )

    private fun setState(newState: ImagePickerState.() -> ImagePickerState) {
        stateObs.set(newState(stateObs.get()))
    }

    fun abortLoad() {
        imageLoader.abortLoadImages()
    }

    override fun getUiState(): ObservableState<ImagePickerState> = stateObs

    override fun loadData(config: ImagePickerConfig) {
        imageLoader.abortLoadImages()
        imageLoader.loadDeviceImages(config, object : ImageLoaderListener {
            override fun onImageLoaded(images: List<Image>, folders: List<Folder>) {
                setState {
                    ImagePickerState(
                        images = images,
                        folders = folders,
                        isLoading = false,
                        isFolder = config.isFolderMode.asSingleEvent()
                    )
                }
            }

            override fun onFailed(throwable: Throwable) {
                setState {
                    ImagePickerState(
                        error = throwable.asSingleEvent()
                    )
                }
            }
        })
    }

    fun onDoneSelectImages(selectedImages: List<Image>?, config: ImagePickerConfig) {

        if (config.showDoneButtonAlways && selectedImages?.size == 0) {
            setState {
                copy(finishPickImage = emptyList<Image>().asSingleEvent())
            }
        }

        if (selectedImages == null || selectedImages.isEmpty())
            return

        setState {
            copy(finishPickImage = selectedImages.filter {
                val file = File(it.path)
                file.exists()
            }.asSingleEvent())
        }
    }

    fun captureImage(fragment: Fragment, config: BaseConfig, requestCode: Int) {
        val context = fragment.requireContext().applicationContext
        val intent = cameraModule.getCameraIntent(fragment.requireContext(), config)
        if (intent == null) {
            Toast.makeText(
                context,
                context.getString(R.string.ef_error_create_image_file),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        fragment.startActivityForResult(intent, requestCode)
    }

    fun finishCaptureImage(context: Context, data: Intent?, config: BaseConfig?) {
        cameraModule.getImage(context, data) { images ->
            if (ConfigUtils.shouldReturn(config!!, true)) {
                setState {
                    copy(finishPickImage = images.orEmpty().asSingleEvent())
                }
            } else {
                setState {
                    copy(showCapturedImage = Unit.asSingleEvent())
                }
            }
        }
    }

    fun abortCaptureImage(context: Context) {
        cameraModule.removeImage(context)
    }
}