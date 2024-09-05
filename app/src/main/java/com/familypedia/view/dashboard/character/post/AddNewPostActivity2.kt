package com.familypedia.view.dashboard.character.post

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
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
import com.familypedia.utils.Constants.REQUEST_IMAGE
import com.familypedia.utils.Constants.SINGLE_DAY_EVENT
import com.familypedia.utils.listeners.ItemClickListener
import com.familypedia.view.dashboard.character.post.adapter.ImageAdapter
import com.familypedia.viewmodels.CharacterViewModel

import java.text.SimpleDateFormat
import java.util.*

class AddNewPostActivity2 : AppCompatActivity(), FamilyPediaClickListener {
    private var date = ""
    private var permissionsRequestCode = 123
    private var imgList: ArrayList<String> = ArrayList()
    private var currentImagePath: String? = null
    private lateinit var imageAdapter: ImageAdapter
    private var characterViewModel: CharacterViewModel? = null
    var characterId: String = ""
    val eventTypeList = arrayListOf(MULTIPLE_DAY_EVENT, SINGLE_DAY_EVENT)
    private var selectedEvent = ""
    private var startingDate = ""
    private var endingDate = ""
    private var location = ""
    private var description = ""
    private lateinit var binding: ActivityAddNewPostBinding
    private var from = ""
    private var postId: String? = null
    private var characterName = ""
    private var characterData: CharacterData? = null
    var serverImageToBeRemoved = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root) // Set the content view using View Binding

        initializeControl()
    }

    private fun initializeControl() {
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
        binding?.spinnerEvent?.setText(selectedEvent)
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
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding?.toolbarAddNewPost?.ivBack -> onBackPressed()
            binding?.etFrom -> openDatePicker(true)
            binding?.etTo -> openDatePicker(false)
            binding?.btnAttachPhoto -> {
                checkPermission()
            }
            binding?.btnLay?.btnContinue -> onContinueClick()
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
            selectedEvent = eventTypeList[i]
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

    private fun openDatePicker(fromSelectFrom: Boolean) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(
            this, { view, year, monthOfYear, dayOfMonth ->
                date = "$dayOfMonth/${monthOfYear + 1}/$year"
                if (fromSelectFrom) {
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
        dpd.show()
    }

    private fun checkPermission() {
        if (((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )) != PackageManager.PERMISSION_GRANTED)
            && (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            )) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    permissionsRequestCode
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    permissionsRequestCode
                )
            }
        } else {
            optionUpload()
        }
    }

    private fun optionUpload() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT;
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.select)),
            REQUEST_IMAGE
        )
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
                binding?.btnLay?.progressBar?.visibility = View.VISIBLE
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
                                this@AddNewPostActivity2,
                                scaledBitmap,
                                uriTemp
                            )
                        currentImagePath =
                            Utility.saveTempBitmap(this@AddNewPostActivity2, rotatedBitmap)

                    } catch (e: Exception) {
                        Log.e("<<<LOG>>>", "Failed to load", e)
                    }
                    return bmp
                }catch (e:Exception){
                    runOnUiThread {
                        toast(getString(R.string.something_went_wrong))
                    }
                }
                return null
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPostExecute(aVoid: Bitmap) {
                super.onPostExecute(aVoid)
                binding?.btnLay?.progressBar?.visibility = View.GONE
                imgList.add(currentImagePath.toString())



                imageAdapter.notifyDataSetChanged()
            }
        }
        Converter().execute()
    }

    private fun onContinueClick() {
        if (validateFields())
            createPost()
    }


    private fun validateFields(): Boolean {
        removeError()
        startingDate = binding?.etFrom?.text.toString()
        endingDate = binding?.etTo?.text.toString()
        location = binding?.etLocation?.text.toString()
        description = binding?.etDescription?.text.toString()

        if (selectedEvent.isEmpty()) {
            binding?.tilEvent?.error = getString(R.string.error_event)
            return false
        } else if (startingDate.isEmpty()) {
            binding?.tilFrom?.error = getString(R.string.error_starting_date)
            return false
        } else if (selectedEvent == eventTypeList[0] && endingDate.isEmpty()) {
            binding?.tilTo?.error = getString(R.string.error_ending_date)
            return false
        } else if (selectedEvent == eventTypeList[0]) {
            if (startingDate == endingDate) {
                binding?.clParent?.showStringSnackbarError("Starting and Ending date can not be same for multiple day event.")
                return false
            }
            val sdf = SimpleDateFormat("dd MMMM yyyy")
            val dateStart: Date = sdf.parse(startingDate)
            val dateEnd: Date = sdf.parse(endingDate)
            if (dateStart.after(dateEnd)) {
                binding?.clParent?.showStringSnackbarError("Ending date can not be before starting date.")
                return false
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
        manageButton(false)

        var endDate = if (selectedEvent == eventTypeList[0])
            endingDate
        else
            ""

        val createPostRequest = CreatePostRequest(
            postId,
            characterId,
            startingDate,
            startingDate,
            endDate,
            selectedEvent,
            description,
            location,
            imgList
        )

        characterViewModel?.createUpdatePost(createPostRequest, this)
    }

    private fun removePostPhoto(index: Int) {
        postId?.let { postId ->
            val request = RemovePostImageRequest(postId, index)
            characterViewModel?.removePostPhotos(this, request)
        }
    }

    private fun responseData() {
        characterViewModel?.simpleResponseResult?.observe(this, { response ->
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
        })
    }

    private fun dataLoadingObserver() {
        characterViewModel?.updateLoaderStatus?.observe(this, {
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
                }
            }
        })
    }

    private fun onGettingException() {
        characterViewModel?.exception?.observe(this, {
            if (it.apiName == CREATE_POST) {
                binding?.clParent?.showStringSnackbarError(it.message)
                revertButtonProgress()
            }
        })
    }

    private fun onGettingError() {
        characterViewModel?.error?.observe(this, {
            if (it.apiName == CREATE_POST) {
                binding?.clParent?.showStringSnackbarError(it.message)
                revertButtonProgress()
            }
        })
    }

    private fun showButtonProgress() {
        binding?.btnLay?.btnContinue?.text = ""
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
        binding?.btnLay?.progressBar?.hideView()
    }

    companion object {
        fun open(currActivity: Activity, bundle: Bundle) {
            val intent = Intent(currActivity, AddNewPostActivity2::class.java)
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