package com.familypedia.view.dashboard.character.addCharacter

import android.app.Activity
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.familypedia.R
import com.familypedia.databinding.ActivityAddNewCharacterBinding
import com.familypedia.network.CharacterData
import com.familypedia.utils.*
import com.familypedia.utils.Constants.ADD_NEW_CHARACTER
import com.familypedia.utils.Constants.CHARACTER_DATA
import com.familypedia.utils.Constants.CHARACTER_NAME
import com.familypedia.utils.Constants.FROM
import com.familypedia.utils.Constants.FROM_ADD_CHARACTER
import com.familypedia.utils.Constants.FROM_EDIT_CHARACTER
import com.familypedia.utils.Constants.IMAGE_URL
import com.familypedia.utils.Constants.STATUS_ALIVE
import com.familypedia.utils.Constants.STATUS_DEAD
import com.familypedia.utils.Utility.formateDate
import com.familypedia.view.dashboard.character.post.AddNewPostActivity
import com.familypedia.viewmodels.CharacterViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddNewCharacterActivity : AppCompatActivity(), FamilyPediaClickListener,
    SuccessFulListenerAddCharacter {
    private val statusList = arrayListOf<String>()
    private var characterViewModel: CharacterViewModel? = null

    //PIC
    private var STORAGE_STORAGE_REQUEST_CODE = 6166
    var isUpdateDone = false
    var noImage: Boolean = false
    private val permissions = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        /*android.Manifest.permission.WRITE_EXTERNAL_STORAGE,*/
        android.Manifest.permission.CAMERA
    )
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissions13 = arrayOf(
        android.Manifest.permission.READ_MEDIA_IMAGES,
        android.Manifest.permission.CAMERA
    )
    private var mCurrentPhotoPath: String? = null
    var body: MultipartBody.Part? = null
    private var dateOfBirth = ""
    private var dateOfDeath = ""
    private var status = ""
    private var countryOfBirth = ""
    private var cityOfBirth = ""
    private var date = ""
    private var characterName = ""
    private var from = ""
    private var bundle: Bundle? = null
    private var characterData: CharacterData? = null
    private var characterId: String = ""
    private var dialogTitle = ""
    private lateinit var binding: ActivityAddNewCharacterBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewCharacterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeControl()
    }

    private fun initializeControl() {
        Utility.setLocale(this)
/*        statusList.add(getString(R.string.alive))
        statusList.add(getString(R.string.dead))*/
        statusList.add(getString(R.string.yes))
        statusList.add(getString(R.string.no))
        getIntentData()
        setViews()
        setSpinner()
        setListeners()
        initViewModel()
        responseData()
        onGettingError()
        onGettingException()
        dataLoadingObserver()

    }

    private fun getIntentData() {
        bundle = intent.extras
        from = bundle?.getString(FROM) ?: return
        characterName = bundle?.getString(CHARACTER_NAME) ?: ""
        if (from == FROM_EDIT_CHARACTER) {
            binding?.tilName?.showView()
            characterData = bundle?.getSerializable(CHARACTER_DATA) as CharacterData?
            setCharacterData(characterData)
            dialogTitle = getString(R.string.edit_character)
        } else {
            binding?.tilName?.hideView()
            dialogTitle = getString(R.string.new_character)
            status = STATUS_ALIVE
            binding?.spinnerStatus?.setText(statusList[0])
            binding?.tilDOD?.hideView()
        }

    }

    private fun setViews() {
        if (from == FROM_ADD_CHARACTER){
            binding?.toolbarAddCharacter?.tvToolbarTitle?.text = getString(R.string.add_new_character_)
            binding?.btnLay?.btnContinue?.text = getString(R.string.save)

        }
        else{
            binding?.toolbarAddCharacter?.tvToolbarTitle?.text = getString(R.string.edit_character)
            binding?.btnLay?.btnContinue?.text = getString(R.string.update)

        }
        binding?.tvCharacterName?.text = characterName
    }

    private fun setCharacterData(characterData: CharacterData?) {
        characterId = characterData?._id ?: return
        characterName = characterData.name ?: ""
        status = characterData.status ?: ""
        dateOfBirth = characterData.date_of_birth ?: ""
        dateOfDeath = characterData.date_of_death ?: ""
        cityOfBirth = characterData.city_of_birth ?: ""
        countryOfBirth = characterData.country_of_birth ?: ""


        if (status == STATUS_ALIVE) {
            binding?.spinnerStatus?.setText(statusList[0])
            binding?.tilDOD?.hideView()
        } else {
            binding?.spinnerStatus?.setText(statusList[1])
        }
        binding?.etDOB?.setText(dateOfBirth)
        binding?.etDOD?.setText(dateOfDeath)
        binding?.etCity?.setText(cityOfBirth)
        binding?.etCountry?.setText(countryOfBirth)
        binding?.ivProfileImage?.loadImagesWithGlide(IMAGE_URL + characterData.profile_pic, false)
        binding?.etName?.setText(characterName)
    }

    private fun setListeners() {
        binding?.etDOB?.familyPediaClickListener(this)
        binding?.etDOD?.familyPediaClickListener(this)
        binding?.toolbarAddCharacter?.ivBack?.familyPediaClickListener(this)
        binding?.btnLay?.btnContinue?.familyPediaClickListener(this)
        binding?.ivProfileImage?.familyPediaClickListener(this)
        binding?.ivCamera?.familyPediaClickListener(this)
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding?.toolbarAddCharacter?.ivBack -> onBackPressed()
            binding?.etDOB -> openDatePicker(true)
            binding?.etDOD -> openDatePicker(false)
            binding?.btnLay?.btnContinue -> onSaveClick()
            binding?.ivProfileImage -> onProfileImageClicked()
            binding?.ivCamera -> onProfileImageClicked()
        }
    }

    private fun openDatePicker(fromDOB: Boolean) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(
            this, { view, year, monthOfYear, dayOfMonth ->
                date = "$dayOfMonth/${monthOfYear + 1}/$year"
                if (fromDOB) {
                    binding?.etDOB?.setText(formateDate(date))
                } else {
                    binding?.etDOD?.setText(formateDate(date))
                }
            },
            year,
            month,
            day
        )
        dpd.datePicker.maxDate = System.currentTimeMillis()
       dpd.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            getString(R.string.accept)) { dialog, which ->
            if (fromDOB) {

                val dayOfMonth = dpd.datePicker.dayOfMonth
                val monthOfYear = dpd.datePicker.month
                val year = dpd.datePicker.year
                date = "$dayOfMonth/${monthOfYear + 1}/$year"
                binding?.etDOB?.setText(formateDate(date))
            } else {
                val dayOfMonth = dpd.datePicker.dayOfMonth
                val monthOfYear = dpd.datePicker.month
                val year = dpd.datePicker.year
                date = "$dayOfMonth/${monthOfYear + 1}/$year"
                binding?.etDOD?.setText(formateDate(date))

            }
        }
        dpd.setButton(
            DialogInterface.BUTTON_POSITIVE,
            getString(R.string.cancel)) { dialog, which ->
            dialog.dismiss()

        }
        dpd.show()

    }

    private fun setSpinner() {
        val statusAdapter = ArrayAdapter(this, R.layout.item_spinner_list, statusList)
        binding?.spinnerStatus?.setAdapter(statusAdapter)
        binding?.spinnerStatus?.setOnItemClickListener { adapterView, view, i, l ->
            if (i == 0) {
                status = STATUS_ALIVE
            } else {
                status = STATUS_DEAD
            }
            if (status == STATUS_ALIVE) {//alive
                binding?.tilDOD?.hideView()
            } else {//dead
                binding?.tilDOD?.showView()
            }
        }
    }

    private fun onSaveClick() {
        if (validateFields()) {
            if (from == FROM_ADD_CHARACTER)
                hitAddNewCharacterAPI(true)
            else
                hitAddNewCharacterAPI(false)
        }
    }

    private fun validateFields(): Boolean {
        dateOfBirth = binding?.etDOB?.text.toString()
        dateOfDeath = binding?.etDOD?.text.toString()
        cityOfBirth = binding?.etCity?.text.toString().trim()
        countryOfBirth = binding?.etCountry?.text.toString().trim()


        binding?.tilStatus?.isErrorEnabled = false
        removeErrors()
        if (from == FROM_EDIT_CHARACTER) {
            characterName = binding?.etName?.text.toString()
            if (characterName.trim().isEmpty()) {
                binding?.tilName?.error = getString(R.string.error_name)
                return false
            }
        } else if (status.isEmpty()) {
            binding?.tilStatus?.error = getString(R.string.error_character_status)
            return false
        } else if (dateOfBirth.isEmpty()) {
            binding?.tilDOB?.error = getString(R.string.error_dob)
            return false
        } else if (status == STATUS_DEAD && dateOfDeath.isEmpty()) {
            binding?.tilDOD?.error = getString(R.string.error_dod)
            return false
        } else if (status == STATUS_DEAD) {
            if (dateOfBirth == dateOfDeath) {
                binding?.clParent?.showStringSnackbarError(getString(R.string.db_and_dd_cant_be_same))
                return false
            }
            val sdf = SimpleDateFormat("dd MMMM yyyy")
            val dob: Date = sdf.parse(dateOfBirth)
            val dod: Date = sdf.parse(dateOfDeath)
            if (dob.after(dod)) {
                binding?.clParent?.showStringSnackbarError(getString(R.string.db_cant_be_after_dod))
                return false
            }
        } else if (cityOfBirth.isEmpty()) {
            binding?.tilCityOfBirth?.error = getString(R.string.error_city_of_birth)
            return false
        } else if (countryOfBirth.isEmpty()) {
            binding?.tilCountryOfBirth?.error = getString(R.string.error_country_of_birth)
            return false
        }
        return true
    }

    private fun removeErrors() {
        binding?.tilName?.isErrorEnabled = false
        binding?.tilStatus?.isErrorEnabled = false
        binding?.tilDOD?.isErrorEnabled = false
        binding?.tilDOB?.isErrorEnabled = false
        binding?.tilCityOfBirth?.isErrorEnabled = false
        binding?.tilCountryOfBirth?.isErrorEnabled = false
    }

    private fun onProfileImageClicked() {
        checkPermission()
    }

    /********************* PROFILE PICTURE **************************************/

    private fun checkStoragePermission() {

        /* if (!Utility.isHasPermission(this, *permissions)) {

             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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
                         STORAGE_STORAGE_REQUEST_CODE,
                         null
                     )
                 } catch (e: java.lang.Exception) {
                     val intent = Intent()
                     intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                     ActivityCompat.startActivityForResult(
                         this,
                         intent,
                         STORAGE_STORAGE_REQUEST_CODE,
                         null
                     )
                 }
             } else {
                 Utility.askPermission(
                     this,
                     permissions = *permissions,
                     requestCode = STORAGE_STORAGE_REQUEST_CODE
                 )
             }
//         } else {*/
//        if (!Utility.isHasPermission2(this, *permissions)) {
//            Utility.askPermission(
//                this,
//                permissions = *permissions,
//                requestCode = STORAGE_STORAGE_REQUEST_CODE
//            )
//        } else
//            optionUpload()


    }
    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            checkPermissionFor13()
        } else {
            checkPermissionForUpto12()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
                            DialogInterface.BUTTON_POSITIVE ->  checkPermission()
                            DialogInterface.BUTTON_NEGATIVE -> dialog.dismiss()
                        }
                    }
                }
            }
        }
    }


    private fun optionUpload() {
        CameraGalleryDialog(this, noImage, object : CameraGalleryListener {
            override fun onCameraClicked() {
                launchCameraIntent(this@AddNewCharacterActivity)
            }

            override fun onGalleryClicked() {
                launchGalleryIntent(this@AddNewCharacterActivity)
            }

            override fun onRemoveImageClicked() {
                noImage = true
                binding?.ivProfileImage?.let {
                    Glide.with(this@AddNewCharacterActivity).load(R.mipmap.ic_profile_pic)
                        .error(R.mipmap.ic_profile_pic)
                        .into(it)
                }
                mCurrentPhotoPath = ""
            }
        }).show()
    }


    fun launchCameraIntent(activity: Activity) {
        val intent = Intent(activity, ImagePickerActivity::class.java)
        intent.putExtra(
            ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION,
            ImagePickerActivity.REQUEST_IMAGE_CAPTURE
        )

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true)
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1) // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1)

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true)
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000)
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000)
        activity.startActivityForResult(
            intent,
            Constants.REQUEST_IMAGE
        )
    }

    fun launchGalleryIntent(activity: Activity) {
        val intent = Intent(activity, ImagePickerActivity::class.java)
        intent.putExtra(
            ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION,
            ImagePickerActivity.REQUEST_GALLERY_IMAGE
        )

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true)
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1) // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1)
        activity.startActivityForResult(
            intent,
            Constants.REQUEST_IMAGE
        )
    }

    private fun manageButton(activateButton: Boolean) {
        binding?.btnLay?.btnContinue?.isClickable = activateButton
    }

    /*************** VIEW-MODEL AND API OBSERVERS **************************************/

    private fun initViewModel() {
        characterViewModel = ViewModelProvider(this).get(CharacterViewModel::class.java)
        characterViewModel?.init()
    }

    private fun hitAddNewCharacterAPI(addCharacter: Boolean) {
        if (!mCurrentPhotoPath.isNullOrEmpty()) {
            val file = File(mCurrentPhotoPath)
            val reqFile = RequestBody.create("image/jpg".toMediaTypeOrNull(), file)
            body = MultipartBody.Part.createFormData("profile_pic", file.name, reqFile)
        }
        val characterNameRequestBody =
            characterName.toRequestBody()
        val dateOfBirthRequestBody =
            dateOfBirth.toRequestBody()
        val dateOfDeathRequestBody = dateOfDeath.toRequestBody()
        val countryOfBirthRequestBody =
            countryOfBirth.toRequestBody()
        val cityOfBirthRequestBody = cityOfBirth.toRequestBody()
        val statusRequestBody = status.toRequestBody()

        val requestPart: MutableMap<String, RequestBody> = HashMap()
        if (!addCharacter) {
            requestPart["_id"] = characterId.toRequestBody()
        }
        requestPart["name"] = characterNameRequestBody
        requestPart["date_of_birth"] = dateOfBirthRequestBody
        requestPart["date_of_death"] = dateOfDeathRequestBody
        requestPart["city_of_birth"] = cityOfBirthRequestBody
        requestPart["country_of_birth"] = countryOfBirthRequestBody
        requestPart["status"] = statusRequestBody
        characterViewModel?.addEditCharacter(addCharacter, body, requestPart, this)
    }

    private fun responseData() {
        characterViewModel?.characterResponseResult?.observe(this) { profileResponse ->
            if (profileResponse.data != null) {
                if (from == FROM_EDIT_CHARACTER) {
                    toast(profileResponse.message ?: "")
                     //characterData?.profile_pic = profileResponse.data.profile_pic
                    //characterData?.name=profileResponse.data.name
                    val returnIntent = Intent()
                    //returnIntent.putExtra(Constants.SELECTED_LIST, )
                    setResult(Activity.RESULT_OK, returnIntent)
                    //AboutCharacterActivity.profileUpdatedObserver.postValue(true)
                    val intent = Intent(Constants.RECEIVER_DATA_UPDATED)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                    finish()
                } else {
                    characterId = profileResponse.data._id ?: ""
                    characterData = profileResponse.data
                    SuccessfulDialogAddCharacter(
                        this,
                        ADD_NEW_CHARACTER,
                        this,
                        dialogTitle,
                        profileResponse.message ?: ""
                    ).show()
                    binding?.btnLay?.progressBar?.hideView()
                    binding?.btnLay?.btnContinue?.text = getString(R.string.save)

                    val intent = Intent(Constants.NEW_CHARACTER_CREATED)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                }
            }
        }
    }

    private fun dataLoadingObserver() {
        characterViewModel?.updateLoaderStatus?.observe(this) {
            if (it.status) {
                window?.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )
                showButtonProgress()
                manageButton(false)
            } else {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
              //  revertButtonProgress()
                manageButton(true)
            }
        }
    }

    private fun onGettingException() {
        characterViewModel?.exception?.observe(this, {
            binding?.clParent?.showStringSnackbarError(it.message)
            revertButtonProgress()
        })
    }

    private fun onGettingError() {
        characterViewModel?.error?.observe(this, {
            binding?.clParent?.showStringSnackbarError(it.message)
            revertButtonProgress()
        })
    }

    private fun showButtonProgress() {
        binding?.btnLay?.btnContinue?.text = ""
        binding?.btnLay?.progressBar?.showView()
        this.hideKeyboard(this)
    }

    private fun revertButtonProgress() {
        binding?.btnLay?.btnContinue?.text = getString(R.string.save)
        binding?.btnLay?.progressBar?.hideView()
    }

    private fun setResult() {
        val resultIntent = Intent();
        //resultIntent.putExtra("NAME OF THE PARAMETER", valueOfParameter);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    /*************************OVERRIDED METHODS ****************************************/

    override fun onUpdateSuccessfully() {
        if (from == FROM_EDIT_CHARACTER) {
            setResult()
        } else {
            //onBackPressed()
            val bundle = Bundle()
            bundle.putString(FROM, Constants.FROM_ADD_POST)
            bundle.putString(Constants.CHARACTER_ID, characterId)
            bundle.putSerializable(CHARACTER_DATA, characterData)

            AddNewPostActivity.open(this, bundle)
        }
    }

    override fun onDialogDismiss() {
        if (from == FROM_EDIT_CHARACTER) {
            setResult()
        } else {
            onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.REQUEST_IMAGE -> {
                    if (resultCode == RESULT_OK) {
                        val uri = data?.getParcelableExtra<Uri>("path")
                        try {
                            uri?.let { uriFile ->
                                mCurrentPhotoPath = Utility.getRealPathFromURI(this, uriFile)
                                mCurrentPhotoPath?.let { imagePath ->
                                    val file = File(imagePath)
                                    noImage = false
                                    binding?.ivProfileImage?.let {
                                        Glide.with(this).load(file)
                                            .error(R.mipmap.ic_profile_pic)
                                            .placeholder(R.mipmap.ic_profile_pic)
                                            .into(it)
                                    }
                                }
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }

            }
        }
    }


    companion object {
        fun open(currActivity: Activity, bundle: Bundle) {
            val intent = Intent(currActivity, AddNewCharacterActivity::class.java)
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