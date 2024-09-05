package com.familypedia.utils.customGallery

import android.app.Activity
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.provider.MediaStore
import com.familypedia.utils.Utility.checkGif
import java.util.ArrayList

class PickerController internal constructor(private val pickerActivity: CustomGalleryActivity) {

    private var addImagePaths = ArrayList<Uri>()

    private val resolver: ContentResolver = pickerActivity.contentResolver
    private var cameraUtil = CameraUtil()
    private var pathDir = ""


    fun takePicture(activity: Activity, saveDir: String) {
        val cameraUtil= CameraUtil()
        cameraUtil.takePictureCamera(activity,saveDir)
    }

    fun setAddImagePath(imagePath: Uri) {
        this.addImagePaths.add(imagePath)
    }


    internal fun displayImage(bucketId: Long?,
                              exceptGif: Boolean?) {
        DisplayImage(bucketId, exceptGif).execute()
    }

    private inner class DisplayImage internal constructor(private val bucketId: Long?,
                                                          internal var exceptGif: Boolean?) : AsyncTask<Void, Void, ArrayList<ImageModal>>() {

        override fun doInBackground(vararg params: Void): ArrayList<ImageModal> {
            return getAllMediaThumbnailsPath(bucketId!!, exceptGif)
        }

        override fun onPostExecute(result: ArrayList<ImageModal>) {
            super.onPostExecute(result)
            pickerActivity.setAdapterData(result)
        }
    }


    private fun getAllMediaThumbnailsPath(id: Long,
                                          exceptGif: Boolean?): ArrayList<ImageModal> {
        val selection = MediaStore.Images.Media.BUCKET_ID + " = ?"
        val bucketId = id.toString()
        val sort = MediaStore.Images.Media._ID + " DESC"
        val selectionArgs = arrayOf(bucketId)

        val images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val c: Cursor?
        c = if (bucketId != "0") {
            resolver.query(images, null, selection, selectionArgs, sort)
        } else {
            resolver.query(images, null, null, null, sort)
        }
        val imageUris = ArrayList<ImageModal>()
        //val imagemodal= ImageModal("",false,true,0,0,false,0)
        //imageUris.add(imagemodal)
        if (c != null) {
            try {

                if (c.moveToFirst()) {
                    setPathDir(c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA)),
                        c.getString(c.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)))
                    do {
                        if (exceptGif!! && checkGif(c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA))))
                            continue
                        val imgId = c.getInt(c.getColumnIndex(MediaStore.MediaColumns._ID))
                        val path = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + imgId)
                        val imageModal= ImageModal(path.toString(),false,false,0,0,true,0)
                        imageUris.add(imageModal)

                    } while (c.moveToNext())
                }
                c.close()
            } catch (e: Exception) {
                if (!c.isClosed) c.close()
            }

        }
        return imageUris
    }

    private fun setPathDir(path: String, fileName: String): String {
        return   path.replace("/" + fileName, "")
    }

    fun getPathDir(bucketId: Long): String {
        if (pathDir == "" || bucketId == 0L)
            pathDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM + "/Camera").absolutePath
        return pathDir
    }


    internal fun getSavePath(): String {
        return cameraUtil.getSavePath()!!
    }

}

