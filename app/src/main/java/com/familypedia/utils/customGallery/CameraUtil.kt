package com.familypedia.utils.customGallery

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraUtil {
    companion object {
        var savePathReal: String? = null
    }


    fun takePictureCamera(activity: Activity, saveDir: String) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(activity.packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile(saveDir) //make a file
                savePathReal = photoFile.absolutePath
            } catch (ex: IOException) {
                ex.printStackTrace()
                // Error occurred while creating the File
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                val uri: Uri
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(activity,
                        activity.applicationContext.packageName + ".utils.provider", photoFile)
                } else {
                    uri = Uri.fromFile(photoFile)
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

                activity.startActivityForResult(takePictureIntent, Define().TAKE_A_PICK_REQUEST_CODE)
            }
        }

    }

    @Throws(IOException::class)
    private fun createImageFile(saveDir: String): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = File(saveDir)
        return File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir      /* directory */
        )
    }
    internal fun getSavePath(): String? {
        return savePathReal
    }

    internal fun setSavePath(savePath: String) {
        savePathReal=savePath
    }
}