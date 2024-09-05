package com.familypedia.view.dashboard.character.post

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ClipData
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.familypedia.R
import com.familypedia.databinding.ActivityAddNewPostBinding
import com.familypedia.network.*
import com.familypedia.utils.*
import com.familypedia.utils.Constants.CHARACTER_DATA
import com.familypedia.utils.Constants.CHARACTER_ID
import com.familypedia.utils.Constants.FROM
import com.familypedia.utils.Constants.FROM_EDIT_POST
import com.familypedia.utils.Constants.MULTIPLE_DAY_EVENT
import com.familypedia.utils.Constants.POST_DATA
import com.familypedia.utils.Constants.RECEIVER_DATA_UPDATED
import com.familypedia.utils.Constants.REQUEST_IMAGE
import com.familypedia.utils.Constants.SINGLE_DAY_EVENT
import com.familypedia.utils.Utility.getRealPathFromURI
import com.familypedia.utils.customGallery.SelectedImage
import com.familypedia.utils.imagepicker.features.*
import com.familypedia.utils.imagepicker.features.cameraonly.CameraOnlyConfig
import com.familypedia.utils.imagepicker.features.common.BaseConfig
import com.familypedia.utils.imagepicker.helper.LocaleManager
import com.familypedia.utils.imagepicker.model.Image
import com.familypedia.utils.imagepicker.others.CustomImagePickerComponents
import com.familypedia.utils.listeners.ItemClickListener
import com.familypedia.view.dashboard.DashboardActivity
import com.familypedia.view.dashboard.character.post.adapter.ImageAdapter
import com.familypedia.viewmodels.CharacterViewModel
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddNewPostActivity : AppCompatActivity(), FamilyPediaClickListener {
    private var date = ""
    private var permissionsRequestCode = 123
    private var imgList: ArrayList<String> = ArrayList()
    private var compressedImageList: ArrayList<String> = ArrayList()
    private var currentImagePath: String? = null
    private lateinit var imageAdapter: ImageAdapter
    private var characterViewModel: CharacterViewModel? = null
    var characterId: String = ""
    val imageSelectionLimit = 5

    //val eventTypeList = arrayListOf(MULTIPLE_DAY_EVENT, SINGLE_DAY_EVENT)
    val eventTypeList = arrayListOf<String>()
    private var STORAGE_STORAGE_REQUEST_CODE = 6166
    private var selectedEvent = ""
    private var startingDate = ""
    private var endingDate = ""
    private var location = ""
    private var description = ""
    private var from = ""
    private var postId: String? = null
    private var characterName = ""
    private var characterData: CharacterData? = null
    var serverImageToBeRemoved = ""
    private lateinit var binding: ActivityAddNewPostBinding
    private val permissions = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        /*android.Manifest.permission.WRITE_EXTERNAL_STORAGE,*/
        android.Manifest.permission.CAMERA
    )
    private val permissions13 = arrayOf(
        android.Manifest.permission.READ_MEDIA_IMAGES,
        android.Manifest.permission.CAMERA
    )
    private var inProgress = false
    private val selectedImagesUriList = arrayListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root) // Set the content view using View Binding

        initializeControl()
    }

    private fun initializeControl() {
        Utility.setLocale(this)
        eventTypeList.add(getString(R.string.multiple_day_event))
        eventTypeList.add(getString(R.string.single_day_event))

        setImageRecyclerView()
        getIntentData()
        setViews()
        setListeners()
        setSpinner()

        initViewModel()
        dataLoadingObserver()
        responseData()
        onGettingError()
        onGettingException()


    }

    private fun getIntentData() {
        characterId = intent.extras?.getString(CHARACTER_ID) ?: return
        from = intent.extras?.getString(FROM) ?: ""
        characterData = intent.extras?.getSerializable(CHARACTER_DATA) as CharacterData
        if (from == FROM_EDIT_POST) {
            val postData = intent.extras?.getSerializable(POST_DATA) as PostData
            setUIData(postData)
        }else{
            selectedEvent = SINGLE_DAY_EVENT
            binding?.spinnerEvent?.setText(getString(R.string.single_day_event ))
            manageEventData(selectedEvent)
        }
        binding?.tvName?.setTextOnTextView(characterData?.name, "")
    }

    private fun setUIData(postData: PostData) {
        postId = postData._id ?: ""
        selectedEvent = postData.eventType ?: ""
        startingDate = postData.startingDate ?: ""
        endingDate = postData.endingDate ?: ""
        location = postData.location ?: ""
        description = postData.description ?: ""

        if (selectedEvent == MULTIPLE_DAY_EVENT) {
            binding?.spinnerEvent?.setText(getString(R.string.multiple_day_event))
        } else if (selectedEvent == SINGLE_DAY_EVENT) {
            binding?.spinnerEvent?.setText(getString(R.string.single_day_event))
        }
        binding?.etFrom?.setText(startingDate)
        binding?.etTo?.setText(endingDate)
        binding?.etLocation?.setText(location)
        binding?.etDescription?.setText(description)
        /*if (selectedEvent == MULTIPLE_DAY_EVENT) {
            tilTo.showView()
            tilFrom.hint=getString(R.string.from_)
        } else if (selectedEvent == SINGLE_DAY_EVENT) {
            tilTo.hideView()
            tilFrom.hint=getString(R.string.date)
        }*/
        manageEventData(selectedEvent)
        postData.attachPhotos?.let {
            imgList.addAll(it)
            imageAdapter?.notifyDataSetChanged()
        }
    }

    private fun setViews() {
        if (from == FROM_EDIT_POST)
            binding?.toolbarAddNewPost?.tvToolbarTitle?.text = getString(R.string.edit_post)
        else
            binding?.toolbarAddNewPost?.tvToolbarTitle?.text = getString(R.string.add_new_post)
            binding?.btnLay?.btnContinue?.text = getString(R.string.submit)
    }

    private fun manageEventData(selectedEvent: String) {
        if (selectedEvent == MULTIPLE_DAY_EVENT) {
            binding?.tilTo?.showView()
            binding?.tilFrom?.hint = getString(R.string.from_)
            binding?.tilFrom?.placeholderText = getString(R.string.select_from)
        } else if (selectedEvent == SINGLE_DAY_EVENT) {
            binding?.tilTo?.hideView()
            binding?.tilFrom?.hint = getString(R.string.date)
            binding?.tilFrom?.placeholderText = getString(R.string.select_date)
        }
    }

    private fun setListeners() {
        binding?.toolbarAddNewPost?.ivBack?.familyPediaClickListener(this)
        binding?.etFrom?.familyPediaClickListener(this)
        binding?.etTo?.familyPediaClickListener(this)
        binding?.btnAttachPhoto?.familyPediaClickListener(this)
        binding?.btnLay?.btnContinue?.familyPediaClickListener(this)
        binding?.btnCrop?.familyPediaClickListener(this)

    }

    override fun onViewClick(view: View) {
        when (view) {
            binding?.toolbarAddNewPost?.ivBack -> onBackPressed()
            binding?.etFrom -> openDatePicker(true)
            binding?.etTo -> openDatePicker(false)
            binding?.btnAttachPhoto -> checkPermission()
            binding?.btnLay?.btnContinue -> onContinueClick()
            binding?.btnCrop -> {
                startCropIntent()
            }
        }
    }

    private fun startCropIntent() {
        val bundle = Bundle()
        bundle.putSerializable("images", selectedImagesUriList)
        //CropPostImagesActivity.open(this, bundle)
        val intent = Intent(this, CropPostImagesActivity::class.java).apply {
            putExtras(bundle)
        }
        cropActivityLauncher.launch(intent)

    }

    private val cropActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // hitCharactersListAPI()
                val fetchedList = result.data?.extras?.get("images") as ArrayList<Uri>

                //val fetchedList = ImagePicker.getImages(result.data) ?: emptyList()
                fetchedList.forEach { uri ->
                    selectedImagesUriList.add(uri)
                    val realPath = getRealPathFromURI(this, uri)
                    realPath?.let { path ->
                        imgList.add(path)
                    }
                }
                imageAdapter.notifyDataSetChanged()
            }
        }


    private fun setImageRecyclerView() {
        binding?.rvPhotos?.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imageAdapter = ImageAdapter(this, imgList, itemClick)
        binding?.rvPhotos?.adapter = imageAdapter
    }

    val itemClick = object : ItemClickListener {
        override fun onItemClick(image: String, position: Int) {
            if (imgList[position].startsWith("profile_pictures")) {
                serverImageToBeRemoved = imgList[position]
                removePostPhoto(position)
            } else {
                imgList.remove(image)
                imageAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun setSpinner() {
        val eventTypeAdapter = ArrayAdapter(this, R.layout.item_spinner_list, eventTypeList)
        binding?.spinnerEvent?.setAdapter(eventTypeAdapter)
        binding?.spinnerEvent?.setOnItemClickListener { adapterView, view, i, l ->

            if (i == 0) {
                selectedEvent = MULTIPLE_DAY_EVENT
            } else if (i == 1) {
                selectedEvent = SINGLE_DAY_EVENT
            }

            manageEventData(selectedEvent)
            /*if (selectedEvent == eventTypeList[0]) {//multipleDayEvent
                tilTo.showView()
                tilFrom.hint=getString(R.string.from_)

            } else {//singleDayEvent
                tilTo.hideView()
                tilFrom.hint=getString(R.string.date)
            }*/
        }

    }

    var startingDateTimeStamp: String = ""
    private fun openDatePicker(fromSelectFrom: Boolean) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(
            this, { view, year, monthOfYear, dayOfMonth ->
                date = "$dayOfMonth/${monthOfYear + 1}/$year"
                if (fromSelectFrom) {
                    startingDateTimeStamp = Utility.formateDate2(date) ?: ""
                    Log.d("TAG", "openDatePicker: $startingDateTimeStamp")

                    binding?.etFrom?.setText(Utility.formateDate(date))
                } else {
                    binding?.etTo?.setText(Utility.formateDate(date))
                }
            },
            year,
            month,
            day
        )
        // dpd.datePicker.minDate = System.currentTimeMillis()
        dpd.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            getString(R.string.accept)) { dialog, which ->
            if (fromSelectFrom) {

                val dayOfMonth = dpd.datePicker.dayOfMonth
                val monthOfYear = dpd.datePicker.month
                val year = dpd.datePicker.year
                date = "$dayOfMonth/${monthOfYear + 1}/$year"
                binding?.etFrom?.setText(Utility.formateDate(date))
            } else {
                val dayOfMonth = dpd.datePicker.dayOfMonth
                val monthOfYear = dpd.datePicker.month
                val year = dpd.datePicker.year
                date = "$dayOfMonth/${monthOfYear + 1}/$year"
                binding?.etTo?.setText(Utility.formateDate(date))

            }
        }
        dpd.setButton(
            DialogInterface.BUTTON_POSITIVE,
            getString(R.string.cancel)) { dialog, which ->
            dialog.dismiss()


        }
        dpd.show()
        dpd.datePicker.minDate = -13576032000000
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            checkPermissionFor13()
        } else {
            checkPermissionForUpto12()
        }
    }

    private  fun checkPermissionFor13(){
        if (!Utility.isHasPermission2(this, *permissions13)) {
            Utility.askPermission(
                this,
                permissions = *permissions13,
                requestCode = STORAGE_STORAGE_REQUEST_CODE
            )
        } else {
            optionUpload()
        }

    }
 private  fun checkPermissionForUpto12(){
     if (!Utility.isHasPermission2(this, *permissions)) {
         Utility.askPermission(
             this,
             permissions = *permissions,
             requestCode = STORAGE_STORAGE_REQUEST_CODE
         )
     } else {
         optionUpload()
     }

 }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                if (Utility.isHasPermission2(this, *permissions)) {
                    optionUpload()
                } else {
                    Utility.showDialogOK(
                        this,
                        getString(R.string.please_allow_permissions)
                    ) { dialog, which ->
                        when (which) {
                            DialogInterface.BUTTON_POSITIVE -> checkPermission()
                            DialogInterface.BUTTON_NEGATIVE -> dialog.dismiss()
                        }
                    }
                }
            }
        }
    }

    //IMAGE PICKER
    private val images = arrayListOf<Image>()
    fun createImagePickerIntent(context: Context, config: BaseConfig): Intent {
        val intent = Intent(
            context,
            com.familypedia.utils.imagepicker.features.ImagePickerActivity::class.java
        )
        when (config) {
            is ImagePickerConfig -> {
                config.language?.run { LocaleManager.language = this }
                intent.putExtra(ImagePickerConfig::class.java.simpleName, config)
            }
            is CameraOnlyConfig -> {
                intent.putExtra(CameraOnlyConfig::class.java.simpleName, config)
            }
        }
        return intent
    }

    private fun createConfig(): ImagePickerConfig {
        val returnAfterCapture = false
        val isSingleMode = false
        val useCustomImageLoader = false
        val folderMode = true
        val includeVideo = false
        val onlyVideo = false
        val isExclude = false

        ImagePickerComponentsHolder.setInternalComponent(
            CustomImagePickerComponents(this, useCustomImageLoader)
        )

        return ImagePickerConfig {

            mode = if (isSingleMode) {
                ImagePickerMode.SINGLE
            } else {
                ImagePickerMode.MULTIPLE // multi mode (default mode)
            }

            language = "es" // Set image picker language
            theme = R.style.Theme_FamilyPedia

            // set whether pick action or camera action should return immediate result or not. Only works in single mode for image picker
            returnMode = if (returnAfterCapture) ReturnMode.ALL else ReturnMode.NONE

            isFolderMode = folderMode // set folder mode (false by default)
            isIncludeVideo = includeVideo // include video (false by default)
            isOnlyVideo = onlyVideo // include video (false by default)
            arrowColor = Color.RED // set toolbar arrow up color
            folderTitle = getString(R.string.choose_photos) // folder selection title
            imageTitle = getString(R.string.tap_to_select) // image selection title
            doneButtonText = getString(R.string.done) // done button text
            showDoneButtonAlways = true // Show done button always or not
            limit = imageSelectionLimit - imgList.size // max images can be selected (99 by default)
            isShowCamera = false // show camera or not (true by default)
            savePath =
                ImagePickerSavePath("Camera") // captured image directory name ("Camera" folder by default)
            savePath = ImagePickerSavePath(
                Environment.getExternalStorageDirectory().path,
                isRelative = false
            ) // can be a full path

            if (isExclude) {
                excludedImages = images.toFiles() // don't show anything on this selected images
            } else {
                selectedImages = images  // original selected images, used in multi mode
            }
        }
    }

    private fun startWithIntent() {
        val intent = createImagePickerIntent(this, createConfig())
        resultLauncherCustomGallery.launch(intent)
    }

    private var resultLauncherCustomGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val fetchedList = ImagePicker.getImages(result.data) ?: emptyList()
                selectedImagesUriList.clear()
                fetchedList.forEach { image ->
                    selectedImagesUriList.add(image.uri)
                    /*val realPath = getRealPathFromURI(this, image.uri)
                    realPath?.let { path ->
                        imgList.add(path)
                    }*/
                }
                //imageAdapter.notifyDataSetChanged()
                startCropIntent()
            }
            imageList = ArrayList()
        }


    private fun optionUpload() {
        if (imgList.size < 5) {
            startWithIntent()

            /*    val intent =
                    Intent(this, CustomGalleryActivity::class.java).apply {
                        putExtra(Constants.COMING_FROM, 1)
                        putExtra(Constants.IMAGE_SELECTION_LIMIT, imageSelectionLimit - imgList.size)
                    }
                resultLauncher.launch(intent)*/

            //  val c= ImagePickerConfig(Imagepickerm)

            /* FishBun.with(this)
                 .setImageAdapter(GlideAdapter())
                 .setMaxCount(imageSelectionLimit - imgList.size)
                 .setMinCount(1)
                 .setPickerSpanCount(5)
                 .setActionBarColor(Color.parseColor("#795548"), Color.parseColor("#5D4037"), false)
                 .setActionBarTitleColor(Color.parseColor("#ffffff"))
                 .setArrayPaths(path)
                 .setAlbumSpanCount(2, 3)
                 .setButtonInAlbumActivity(false)
                 .setCamera(true)
                 .exceptGif(true)
                 .setReachLimitAutomaticClose(true)
                 .setHomeAsUpIndicatorDrawable(
                     ContextCompat.getDrawable(
                         this,
                         R.drawable.ic_custom_back_white
                     )
                 )
                 .setDoneButtonDrawable(ContextCompat.getDrawable(this, R.drawable.ic_custom_ok))
                 .setAllDoneButtonDrawable(ContextCompat.getDrawable(this, R.drawable.ic_custom_ok))
                 .setIsUseAllDoneButton(ContextCompat.getDrawable(this, R.drawable.ic_custom_ok))
                 .setAllViewTitle("All")
                 .setMenuAllDoneText("All Done")
                 .setActionBarTitle("FishBun Dark")
                 .textOnNothingSelected("Please select three or more!")
                 .exceptMimeType(listOf(MimeType.GIF))
                 .setSpecifyFolderList(arrayListOf("Screenshots", "Camera"))
                 .startAlbumWithOnActivityResult(111)

             */
        } else {
            binding?.clParent?.showStringSnackbar(getString(R.string.image_restrict))
        }

    }

    private var selectedImage = ArrayList<SelectedImage>()
    private var imageList = ArrayList<String>()
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImage =
                    result.data?.getParcelableArrayListExtra(Constants.SELECTED_LIST) ?: ArrayList()
                if (selectedImage.isNotEmpty()) {
                    Log.e("<<NextClick1>>", "clickeTwo")
                    selectedImage.forEach {
                        println("IMAGE_URL ${it.imageUrl}")

                        val realPath = getRealPathFromURI(this, Uri.parse(it.imageUrl))
                        realPath?.let { path ->
                            imgList.add(path)
                        }

                    }
                    imageAdapter.notifyDataSetChanged()
                }

                imageList = ArrayList()
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_IMAGE -> {
                    try {
                        for (image in imgList) {
                            if (!image.startsWith("profile_pictures")) {
                                imgList.remove(image)
                            }
                        }
                    } catch (e: Exception) {

                    }
                    // imgList?.clear()
                    val clipData = data.clipData
                    if (data.clipData != null) {
                        for (i in 0 until clipData!!.itemCount) {
                            if (i > 4 - imgList.size) {
                                //Utility.showSnackbar(llAttach, "Max 5 pictures can be uploaded")
                                shortToast("Max 5 pictures can be uploaded")
                                break
                            } else {
                                val item: ClipData.Item = clipData.getItemAt(i)
                                val uri: Uri = item.uri
                                grabImage(uri)
                            }
                        }
                    }

                }
            }
        }
    }


    private fun grabImage(uriTemp: Uri) {
        class Converter : AsyncTask<Void, Void, Bitmap>() {
            override fun onPreExecute() {
                super.onPreExecute()
              //  progressBar?.visibility = View.VISIBLE
            }

            override fun doInBackground(vararg params: Void): Bitmap? {
                try {
                    contentResolver.notifyChange(uriTemp, null)
                    val cr = contentResolver
                    var bmp: Bitmap? = null
                    try {
                        bmp = MediaStore.Images.Media.getBitmap(cr, uriTemp)
                        val scaledBitmap = Utility.scaleDown(bmp, 300f, true)
                        val rotatedBitmap =
                            Utility.rotateRequiredImage(
                                this@AddNewPostActivity,
                                scaledBitmap,
                                uriTemp
                            )
                        currentImagePath =
                            Utility.saveTempBitmap(this@AddNewPostActivity, rotatedBitmap)

                    } catch (e: Exception) {
                        Log.e("<<<LOG>>>", "Failed to load", e)
                    }
                    return bmp
                } catch (e: Exception) {
                    runOnUiThread {
                        toast(getString(R.string.something_went_wrong))
                    }
                }
                return null
            }

            override fun onPostExecute(aVoid: Bitmap) {
                super.onPostExecute(aVoid)
                binding.btnLay.progressBar?.visibility = View.GONE
                imgList.add(currentImagePath.toString())
                imageAdapter.notifyDataSetChanged()
            }
        }
        Converter().execute()
    }

    private fun onContinueClick() {
        if (validateFields()) {
            //manageButton(false)
            //createPost()
            compress()
        }
    }

    private fun compress() {
        showButtonProgress()
        if (imgList.size > 0) {
            imgList.forEach { image ->
                if (!image.startsWith("profile_pictures")) {
                    lifecycleScope.launch {
                        val compressedImageFile: File = Compressor.compress(
                            this@AddNewPostActivity,
                            File(image),
                            Dispatchers.Main
                        )
                        compressedImageList.add(compressedImageFile.absolutePath)

                        println("compressed ${compressedImageFile}")
                    }
                } else {
                    compressedImageList.add(image)
                }
            }
            createPost()
        } else {
            createPost()
        }
    }


    private fun validateFields(): Boolean {
        removeError()
        startingDate = binding?.etFrom?.text.toString()
        endingDate = binding?.etTo?.text.toString()
        location = binding?.etLocation?.text.toString().trim()
        description = binding?.etDescription?.text.toString().trim()

        if (selectedEvent.isEmpty()) {
            binding?.tilEvent?.error = getString(R.string.error_event)
            return false
        } else if (startingDate.isEmpty()) {
            binding?.tilFrom?.error = getString(R.string.error_starting_date)
            return false
        } else if (selectedEvent == "Multiple Day Event" && endingDate.isEmpty()) {
            binding?.tilTo?.error = getString(R.string.error_ending_date)
            return false
        } else if (selectedEvent == "Multiple Day Event") {
            if (startingDate == endingDate) {
                binding?.clParent?.showStringSnackbarError(getString(R.string.start_end_date_cannot_be_same))
                return false
            }
            try {
                val sdf = SimpleDateFormat("dd MMMM yyyy")
                val dateStart: Date = sdf.parse(startingDate)
                val dateEnd: Date = sdf.parse(endingDate)
                if (dateStart.after(dateEnd)) {
                    binding?.clParent?.showStringSnackbarError(getString(R.string.end_date_before_start_date_error))
                    return false
                }
            } catch (e: Exception) {
            }
        }
        if (location.isEmpty()) {
            binding?.tilLocation?.error = getString(R.string.error_location)
            return false
        } else if (description.isEmpty()) {
            binding?.tilDescription?.error = getString(R.string.error_description)
            return false
        }
        return true
    }

    private fun removeError() {
        binding?.tilEvent?.isErrorEnabled = false
        binding?.tilFrom?.isErrorEnabled = false
        binding?.tilTo?.isErrorEnabled = false
        binding?.tilLocation?.isErrorEnabled = false
        binding?.tilDescription?.isErrorEnabled = false

    }

    /*************** VIEW-MODEL AND API OBSERVERS **************************************/

    private fun initViewModel() {
        characterViewModel = ViewModelProvider(this).get(CharacterViewModel::class.java)
        characterViewModel?.init()
    }

    private fun createPost() {
        if (!inProgress) {
            inProgress = true
            manageButton(false)
            var endDate = ""
            if (selectedEvent == MULTIPLE_DAY_EVENT) {
                endDate = endingDate
            } else {
                endDate = ""
            }

            startingDateTimeStamp = Utility.formateDate3(startingDate) ?: ""
            val createPostRequest = CreatePostRequest(
                postId,
                characterId,
                startingDate,
                startingDateTimeStamp,
                endDate,
                selectedEvent,
                description,
                location,
                compressedImageList
            )

            characterViewModel?.createUpdatePost(createPostRequest, this)
        }
    }

    private fun removePostPhoto(index: Int) {
        postId?.let { postId ->
            val request = RemovePostImageRequest(postId, index)
            characterViewModel?.removePostPhotos(this, request)
        }
    }

    private fun responseData() {
        characterViewModel?.createUpdatePostResult?.observe(this) { response ->
            if (response != null) {
                if (from == FROM_EDIT_POST) {
                    val returnIntent = Intent()
                    returnIntent.putExtra(Constants.SELECTED_LIST, response.data)
                    setResult(Activity.RESULT_OK, returnIntent)
                    val intent = Intent(RECEIVER_DATA_UPDATED)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

                    //AboutCharacterActivity.profileUpdatedObserver.postValue(true)
                    finish()
                } else {
                    shortToast(response.message ?: "")
                    val bundle = Bundle()
                    bundle.putString(FROM, Constants.FROM_SPLASH)
                    finishAffinity()
                    DashboardActivity.open(this, bundle)
                }
                inProgress = false
            }
        }

        characterViewModel?.simpleResponseResult?.observe(this) { response ->
            if (response != null) {
                if (response.apiName == CREATE_POST) {
                    shortToast(response.message ?: "")
                    onBackPressed()
                } else if (response.apiName == REMOVE_POST_PHOTOS) {
                    if (serverImageToBeRemoved.isNotEmpty()) {
                        imgList.remove(serverImageToBeRemoved)
                        imageAdapter.notifyDataSetChanged()
                    }
                    shortToast(response.message ?: "")
                }
            }
        }
    }

    private fun dataLoadingObserver() {
        characterViewModel?.updateLoaderStatus?.observe(this) {
            if (it.apiName == CREATE_POST) {
                if (it.status) {
                    window?.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                    manageButton(false)
                    showButtonProgress()
                } else {
                    window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    manageButton(true)
                    revertButtonProgress()
                    inProgress = false
                }
            } else if (it.apiName == REMOVE_POST_PHOTOS) {
                if (it.status) {
                    window?.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                    binding?.progressRemoveImage?.showView()
                } else {
                    window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    binding?.progressRemoveImage?.hideView()
                }
            }
        }
    }

    private fun onGettingException() {
        characterViewModel?.exception?.observe(this) {
            if (it.apiName == CREATE_POST) {
                binding?.clParent?.showStringSnackbarError(it.message)
                revertButtonProgress()
                inProgress = false
                compressedImageList.clear()
            }
        }
    }

    private fun onGettingError() {
        characterViewModel?.error?.observe(this) {
            if (it.apiName == CREATE_POST) {
                binding?.clParent?.showStringSnackbarError(it.message)
                revertButtonProgress()
                inProgress = false
                compressedImageList.clear()
            }
        }
    }

    private fun showButtonProgress() {
        binding?.btnLay?.btnContinue?.text = ""
        binding?.btnLay?.btnContinue?.isClickable = false
        binding?.btnLay?.progressBar?.showView()
        this.hideKeyboard(this)
    }

    private fun manageButton(activateButton: Boolean) {
        binding?.btnLay?.btnContinue?.isClickable = activateButton
        if (activateButton)
            binding?.btnLay?.btnContinue?.alpha = 1f
        else
            binding?.btnLay?.btnContinue?.alpha = 0.4f
    }

    private fun revertButtonProgress() {
        binding?.btnLay?.btnContinue?.text = getString(R.string.submit)
        binding?.btnLay?.btnContinue?.isClickable = true
        binding?.btnLay?.progressBar?.hideView()
    }

    companion object {
        fun open(currActivity: Activity, bundle: Bundle) {
            val intent = Intent(currActivity, AddNewPostActivity::class.java)
            intent.putExtras(bundle)
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left)
    }
}