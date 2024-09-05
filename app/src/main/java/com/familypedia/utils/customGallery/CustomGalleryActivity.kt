package com.familypedia.utils.customGallery

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.test.core.app.ApplicationProvider
import com.familypedia.R
import com.familypedia.databinding.ActivityCustomGalleryBinding
import com.familypedia.utils.Constants
import com.familypedia.utils.Utility
import java.io.File

class CustomGalleryActivity : AppCompatActivity() ,CustomGalleryAdapter.ImageCountCallBack{
    private var define = Define()
    private var selectcount: Int = 0
    private var comingFrom: Int = 0
    private lateinit var mBinding: ActivityCustomGalleryBinding
    private lateinit var itemAdapter: CustomGalleryAdapter
    private lateinit var pickerController: PickerController
    private var selectedImageList = ArrayList<ImageModal>()
    private val imageUris = ArrayList<ImageModal>()
    private var imageSelectionLimit=5
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_custom_gallery)
        setData()
        setAdapter()
    }
    private fun updateData() {
        var totalSize = 0
        //  selectedImageList = intent.getParcelableArrayListExtra(Constants.SELECTED_LIST)
        if (selectedImageList.size > 0) {
            for (i in selectedImageList.indices) {
                if (!TextUtils.isEmpty(selectedImageList[i].imageUrl)) {
                    if (selectedImageList[i].iseditable) {
                        selectcount += 1
                        imageUris[selectedImageList[i].imagePosition].isSelected = true
                        imageUris[selectedImageList[i].imagePosition].countSelect = selectcount
                    } else {
                        totalSize += 1
                    }
                }
            }
        }
        itemAdapter.updateImageCount(selectcount, totalSize)
        itemAdapter.notifyDataSetChanged()
        if (selectcount > 0) {
            mBinding.tvSelectedCount.text =
                StringBuilder().append(" ").append(selectcount.toString()).append(" ").append("/ 5")
            mBinding.toolbar.tvDone.visibility = View.VISIBLE
        } else {
            mBinding.tvSelectedCount.text = StringBuilder().append(" ").append(getString(R.string.multiple))
            mBinding.toolbar.tvDone.visibility = View.INVISIBLE
        }
    }
    private val permissions = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.CAMERA
    )
    private fun setAdapter() {
        val linearLayoutManager = GridLayoutManager(this, 3)
        mBinding. rvGallery.layoutManager = linearLayoutManager
        itemAdapter = CustomGalleryAdapter(
            this,
            mBinding.llParentGallery,
            pickerController,
            pickerController.getPathDir(0),
            imageSelectionLimit
        )
      //  mBinding.rvGallery.adapter = itemAdapter

      /*   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
             if (!Utility.isHasPermission(this, *permissions)) {

                 try {
                     val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                     intent.addCategory("android.intent.category.DEFAULT")
                     intent.data =
                         Uri.parse(
                             String.format(
                                 "package:%s",
                                 ApplicationProvider.getApplicationContext<Context>()
                                     .getPackageName()
                             )
                         )
                     ActivityCompat.startActivityForResult(
                         this,
                         intent,
                         28,
                         null
                     )
                 } catch (e: java.lang.Exception) {
                     val intent = Intent()
                     intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                     ActivityCompat.startActivityForResult(
                         this,
                         intent,
                         28,
                         null
                     )
                 }
             }else{
                 val pickerController = PickerController(this)
                 pickerController.displayImage(0, true)
             }

        }else {*/
             if (checkPermission()) {
                 pickerController.displayImage(0, true)
             }
    //    }

    }

    fun setAdapterData(result: ArrayList<ImageModal>) {
        imageUris.clear()
        imageUris.addAll(result)
        itemAdapter.setData(imageUris)
        itemAdapter.notifyDataSetChanged()
        if (comingFrom == 1) {
            updateData()
        }
    }

    private fun setData() {
        Utility.changeStatusBarColor(this,R.color.color_blue)
        pickerController = PickerController(this)
        comingFrom = intent.getIntExtra(Constants.COMING_FROM, 0)
        imageSelectionLimit = intent.getIntExtra(Constants.IMAGE_SELECTION_LIMIT, 5)


        mBinding.toolbar.tvHeaderGallery.text = getString(R.string.photos)
        mBinding.toolbar.tvDone.text = getString(R.string.done)
        mBinding.toolbar.tvDone.visibility = View.INVISIBLE


        mBinding.toolbar.ivLeftGallery.setOnClickListener {
            val returnIntent = Intent()
            setResult(Activity.RESULT_CANCELED, returnIntent)
            finish()
        }
        mBinding.toolbar.tvDone.setOnClickListener {
            Log.e("<<NextClick>>", "clickedOnce")
            if (comingFrom == 1) {
                setResultData()
            } else {
                setIntentData()
            }
        }


    }

    private fun setResultData() {
        val selectedList = ArrayList<SelectedImage>()
        for (i in selectedImageList.indices) {
            if (selectedImageList[i].iseditable && !TextUtils.isEmpty(selectedImageList[i].imageUrl)&&selectedImageList[i].imageId!=0) {
                val selectedImage = SelectedImage(
                    selectedImageList[i].imageUrl,
                    i,
                    false,
                    selectedImageList[i].imageId
                )
                selectedList.add(selectedImage)
                //  selectcount += 1
            }
        }
        for (i in imageUris.indices) {
            if (selectedList.size < selectcount) {
                if (imageUris[i].isSelected&&imageUris[i].imageId==0&&!TextUtils.isEmpty(imageUris[i].imageUrl)) {
                    val selectedImage = SelectedImage(imageUris[i].imageUrl, i, true,0)
                    selectedList.add(selectedImage)
                }
            /*    if (selectcount == selectedList.size) {
                    val returnIntent = Intent()
                    returnIntent.putParcelableArrayListExtra(Constants.SELECTED_LIST, selectedList)
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                    break
                }*/
            } else {
               /* val returnIntent = Intent()
                returnIntent.putParcelableArrayListExtra(Constants.SELECTED_LIST, selectedList)
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
                break*/
            }
        }

    }

    private fun setIntentData() {
        val selectedList = ArrayList<SelectedImage>()
        for (i in imageUris.indices) {
            if (selectedList.size < selectcount) {
                if (imageUris[i].isSelected) {
                    val selectedImage =
                        SelectedImage(imageUris[i].imageUrl, i, true,imageUris[i].imageId)
                    selectedList.add(selectedImage)
                }
                if (selectcount == selectedList.size) {
                    break
                }
            } else {
                break
            }
        }

    }

    private fun checkPermission(): Boolean {
        val permissionCheck = PermissionCheck(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionCheck.checkGalleryPermission())
                return true
        } else
            return true
        return false
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == define.TAKE_A_PICK_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val savedFile = File(pickerController.getSavePath())
                SingleMediaScanner(this, savedFile)
                addImage(savedFile)
                val contentURI = Utility.getImageContentUri(this@CustomGalleryActivity, savedFile)
                Log.e("ImageContentURi", contentURI.toString())
            } else {
                File(pickerController.getSavePath()).delete()
            }
        }
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val result = data!!.getIntExtra(Constants.TRANSACTION, 0)
                if (result == 1) {
                    val returnIntent = Intent()
                    returnIntent.putExtra(Constants.TRANSACTION, result)
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                    overridePendingTransition(0, 0)
                } else if (result == 2) {
                    val rejectList = data.getIntegerArrayListExtra(Constants.REJECT_LIST)
                    if (rejectList != null && rejectList.size > 0) {
                        itemAdapter.updateAdapter(rejectList)
                        itemAdapter.notifyDataSetChanged()
                    }
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }

    }

    private fun addImage(path: File) {
        val imagemodal = ImageModal(Utility.getImageContentUri(this@CustomGalleryActivity, path).toString(),
            false,
            false,
            0,
            0,
            true,
            0
        )
        imageUris.add(1, imagemodal)
        itemAdapter.notifyDataSetChanged()
        pickerController.setAddImagePath(Uri.fromFile(path))
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            28 -> {
                if (grantResults.isNotEmpty()) {
                    if (grantResults.size == 2) {
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                            val pickerController = PickerController(this)
                            pickerController.displayImage(0, true)
                        } else {
                            //checkPermission()
                            PermissionCheck(this).showPermissionDialog()
                            // finish()
                        }
                    } else {
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            val pickerController = PickerController(this)
                            pickerController.displayImage(0, true)
                        } else {
                            //checkPermission()
                            PermissionCheck(this).showPermissionDialog()
                            // finish()
                        }
                    }
                }
            }
        }
    }

    override fun imageCount(count: Int) {
        selectcount = count
        if (count > 0) {
            mBinding.tvSelectedCount.text =
                StringBuilder().append(" ").append(count.toString()).append(" ").append("/ 5")
            mBinding.toolbar.tvDone.visibility = View.VISIBLE
        } else {
            mBinding.tvSelectedCount.text = StringBuilder().append(" ").append(getString(R.string.multiple))
            mBinding.toolbar.tvDone.visibility = View.INVISIBLE
        }
    }

  /*  override fun onBackPressed() {
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()

    }*/
}