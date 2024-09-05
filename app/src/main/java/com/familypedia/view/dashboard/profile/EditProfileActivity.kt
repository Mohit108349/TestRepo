package com.familypedia.view.dashboard.profile

import android.app.Activity
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.familypedia.R
import com.familypedia.databinding.ActivityEditProfileBinding
import com.familypedia.network.LoadingData
import com.familypedia.network.ResendOtpRequest
import com.familypedia.utils.*
import com.familypedia.utils.Constants.COUNTRY_CODE
import com.familypedia.utils.Constants.FROM_EDIT_PROFILE
import com.familypedia.utils.Constants.IMAGE_URL
import com.familypedia.utils.Constants.REQUEST_IMAGE
import com.familypedia.utils.Utility.askPermission
import com.familypedia.utils.Utility.formateDate
import com.familypedia.utils.Utility.isHasPermission2
import com.familypedia.utils.Utility.isValidEmail
import com.familypedia.utils.Utility.saveUserDetailsInPreferences
import com.familypedia.utils.Utility.showDialogOK
import com.familypedia.view.auth.AuthViewsHolderActivity
import com.familypedia.viewmodels.AuthViewModel
import com.familypedia.viewmodels.ProfileViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.util.*

class EditProfileActivity : AppCompatActivity(), FamilyPediaClickListener {
    val genderList = arrayListOf<String>()
    private var date = ""

    //PIC0
    private var STORAGE_STORAGE_REQUEST_CODE = 6166
    var isUpdateDone = false
    var noImage: Boolean = false
    private val permissions = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        /*android.Manifest.permission.WRITE_EXTERNAL_STORAGE*/
        android.Manifest.permission.CAMERA
    )
    private val permissions13 = arrayOf(
        android.Manifest.permission.READ_MEDIA_IMAGES,
        android.Manifest.permission.CAMERA
    )
    private val permissions_alt = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        /*android.Manifest.permission.WRITE_EXTERNAL_STORAGE*/
        android.Manifest.permission.CAMERA
    )
    private var mCurrentPhotoPath: String? = null
    private var uriTemp: Uri? = null
    private var profileViewModel: ProfileViewModel? = null
    var body: MultipartBody.Part? = null
    var gender = ""
    private var resultLauncher: ActivityResultLauncher<Intent>? = null
    private var isPhoneVerified = false
    private var isEmailVerified = false
    private var orignalNumber = false
    private var authViewModel: AuthViewModel? = null
    private var authType = ""
    private var newEmail = ""
    private var newPhone = ""
    private var newCountryCode = ""
    private var VERIFY_AUTH_CODE = 101
    var originalEmail = ""
    var originalPhone = ""
    private lateinit var binding: ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeControl()
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // hitCharactersListAPI()
                }
            }
    }

    private fun initializeControl() {
        genderList.add(getString(R.string.male))
        genderList.add(getString(R.string.female))
        genderList.add(getString(R.string.others))
        binding.etPhoneNumber.inputType = InputType.TYPE_CLASS_NUMBER
        Utility.setLocale(this)
        setupViews()
        setupListeners()
        setSpinner()
        initViewModel()
        responseData()
        onGettingError()
        onGettingException()
        dataLoadingObserver()

    }

    private fun setupListeners() {
        binding.toolbarEditProfile.ivBack.familyPediaClickListener(this)
        binding.ivProfileImage.familyPediaClickListener(this)
        binding.btnLay?.btnContinue?.familyPediaClickListener(this)
        binding.etDOB.familyPediaClickListener(this)
        binding.ivCamera.familyPediaClickListener(this)
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding.toolbarEditProfile?.ivBack -> onBackPressed()
            binding.ivProfileImage -> checkPermission()
            binding.ivCamera -> checkPermission()
            binding.btnLay?.btnContinue -> onSaveClick()
            binding.etDOB -> openDatePicker(true)
            //binding.etDOD -> openDatePicker(false)

        }
    }

    private fun setupViews() {
        binding.toolbarEditProfile.tvToolbarTitle.text = getString(R.string.edit_profile)
        binding.btnLay.btnContinue.text = getString(R.string.save)
        originalPhone = Utility.getPreferencesString(this, Constants.PHONE_NUMBER)
        originalEmail = Utility.getPreferencesString(this, Constants.EMAIL)
        if (originalPhone.isNotEmpty()) {
            isPhoneVerified = true
        }
        if (originalEmail.isNotEmpty()) {
            isEmailVerified = true
        }
        binding.tvUserName.setTextOnTextView(
            Utility.getPreferencesString(this, Constants.USER_NAME),
            getString(R.string.n_a)
        )
        binding.etName.setTextOnEditText(Utility.getPreferencesString(this, Constants.USER_NAME), "")
        val valueToSet = Utility.getPreferencesString(this, Constants.EMAIL)
        if (valueToSet.isNotBlank()) {
            binding. tvUserEmail.setTextOnTextView(
                Utility.getPreferencesString(this, Constants.EMAIL),
                getString(R.string.n_a)
            )
        } else {
            val phoneValueToSet =
                Utility.getPreferencesString(this, Constants.PHONE_NUMBER)
            if (phoneValueToSet.isNotBlank()) {
                binding.tvUserEmail.setTextOnTextView(
                    Utility.getPreferencesString(this, Constants.PHONE_NUMBER),
                    getString(R.string.n_a)
                )
            }
        }
//        tvUserEmail.setTextOnTextView(
//            Utility.getPreferencesString(this, Constants.LOGIN_FROM),
//            getString(R.string.n_a)
//        )
        var countryCode = Utility.getPreferencesString(this, COUNTRY_CODE)
        if (countryCode.isNotEmpty()) {
            if (countryCode.contains("+"))
                countryCode = countryCode.removeRange(0, 1)
            //binding.codePicker.setCountryForPhoneCode(countryCode.toInt())
        }

//        isPhoneVerified = Utility.getPreferencesBoolean(this,Constants.IS_PHONE_VERFIED)
//        verifyPhoneNumber.isVisible = !isPhoneVerified
//        isEmailVerified = Utility.getPreferencesBoolean(this,Constants.IS_EMAIL_VERIFIED)
//        verifyEmail.isVisible = !isEmailVerified

        binding.etPhoneNumber.setTextOnEditText(
            Utility.getPreferencesString(this, Constants.PHONE_NUMBER),
            ""
        )
        var phoneNumber = Utility.getPreferencesString(this, Constants.PHONE_NUMBER)
        binding.etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int,
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int,
            ) {
                binding. verifyPhoneNumber.isVisible = phoneNumber != binding.etPhoneNumber.text.toString()
                binding. tilPhoneNumber.isErrorEnabled = false
                isPhoneVerified = s.toString() == originalPhone
            }
        })
        binding.etEmail.setTextOnEditText(Utility.getPreferencesString(this, Constants.EMAIL), "")
        var email = Utility.getPreferencesString(this, Constants.EMAIL)
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int,
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int,
            ) {

                binding.verifyEmail.isVisible = email != binding.etEmail.text.toString()
                binding.tilEmail.isErrorEnabled = false
                isEmailVerified = s.toString() == originalEmail
            }
        })

        binding.verifyEmail.setOnClickListener {
            val enailNew = binding.etEmail.text.toString().trim()
            if (enailNew.isEmpty()) {
                binding.tilEmail.error = getString(R.string.error_email_empty)
            } else if (!isValidEmail(enailNew)) {
                binding. tilEmail.error = getString(R.string.error_email_invalid)

            } else {
                val resendOtpRequest = ResendOtpRequest(enailNew, null, null)
                binding. tilEmail.isErrorEnabled = false
                authType = Constants.EMAIL
                newEmail = enailNew
                profileViewModel?.verifyAUthType(resendOtpRequest, this@EditProfileActivity)

            }
        }

        binding.verifyPhoneNumber.setOnClickListener {
            val phoneNew = binding.etPhoneNumber.text.toString().trim()
            //val cc = binding.codePicker.selectedCountryCode
            if (phoneNew.isEmpty()) {
                binding.tilPhoneNumber.error = getString(R.string.error_phone)

            } else if (phoneNew.length < 7) {
                binding.tilPhoneNumber.error = getString(R.string.error_phone)

            } else {
                val resendOtpRequest = ResendOtpRequest(null, "", phoneNew)
                binding.tilPhoneNumber.isErrorEnabled = false
                authType = Constants.PHONE_NUMBER
                newPhone = phoneNew
                newCountryCode = ""
                profileViewModel?.verifyAUthType(resendOtpRequest, this@EditProfileActivity)
            }
        }

        Utility.getPreferencesString(this, Constants.DATE_OF_BIRTH).let { dob ->
            if (dob.isNotEmpty())
                binding. etDOB.setTextOnEditText(dob, "")
            else
                binding.tilDOB.placeholderText = getString(R.string.select_dob)
        }
        binding.etCountry.setTextOnEditText(
            Utility.getPreferencesString(this, Constants.COUNTRY),
            ""
        )
        binding.etCity.setTextOnEditText(
            Utility.getPreferencesString(this, Constants.CITY),
            ""
        )
        Utility.getPreferencesString(this, Constants.GENDER).let { gender ->
            if (gender.isNotEmpty()) {
                binding. spinnerGender.setText(gender)
                this.gender = gender
            } else
                binding. tilGender.placeholderText = getString(R.string.select_gender)
        }

        binding. ivProfileImage.loadImagesWithGlide(
            IMAGE_URL + Utility.getPreferencesString(
                this,
                Constants.PROFILE_PIC
            ), false
        )

    }

    private fun setSpinner() {
        val genderAdapter = ArrayAdapter(this, R.layout.item_spinner_list, genderList)
        binding.spinnerGender.setAdapter(genderAdapter)
        binding.spinnerGender.setOnItemClickListener { adapterView, view, i, l ->
            gender = genderList[i]
        }
    }

    private fun validateFields(): Boolean {
        binding.tilGender.isErrorEnabled = false
        binding.tilPhoneNumber.isErrorEnabled = false
        binding.tilName.isErrorEnabled = false
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val name = binding.etName.text.toString().trim()
        /*if (gender.isEmpty()) {
            tilGender.error = getString(R.string.error_gender)
            return false
        }*/
        if (name.trim().isEmpty()) {
            binding.tilName.error = getString(R.string.error_name)
            return false
        }

        if (phoneNumber.isBlank() && email.isBlank()) {
            binding.tilPhoneNumber.error = getString(R.string.phone_number_cant_be_empty)
            binding.tilEmail.error = getString(R.string.email_cant_be_empty)
            return false
        }
        else if (phoneNumber.isNotBlank()) {
            if (email.isNotBlank()){
                if (!isEmailVerified){
                    binding.tilEmail.error = getString(R.string.email_is_not_verified)
                    return false
                }
                if (isValidEmail(email)){
                    binding.tilEmail.error = getString(R.string.error_email_invalid)
                    return false
                }
            }

            if (originalPhone == phoneNumber) {
                return true
            }
            if (phoneNumber.length < 7) {
                binding.tilPhoneNumber.error = getString(R.string.error_phone)
                return false
            }
            if (binding.verifyPhoneNumber.isVisible){
                binding.tilPhoneNumber.error = getString(R.string.phone_number_is_not_verified)
                return false
            }
            return isPhoneVerified
        }

        if (email.isNotBlank()) {
            if (originalEmail == email) {
                return true
            }
            if (isValidEmail(email)) {
                binding.tilEmail.error = getString(R.string.error_email_invalid)
                return false
            }
            if (binding.verifyEmail.isVisible){
                binding.tilEmail.error = getString(R.string.email_is_not_verified)
                return false
            }
            return isEmailVerified
        }

        /*       else if (phoneNumber.isEmpty() && originalPhone.isEmpty()) {
                   tilPhoneNumber.error = getString(R.string.phone_number_cant_be_empty)
                   return false
               } else if (phoneNumber.isNotEmpty() && phoneNumber.length < 7) {
                   tilPhoneNumber.error = getString(R.string.error_phone)
                   return false
               } else if (email.isEmpty() && originalEmail.isNotEmpty()) {
                   tilEmail.error = getString(R.string.email_cant_be_empty)
                   return false
               } else if (email.isNotEmpty() && !Utility.isValidEmail(email)) {
                   tilEmail.error = getString(R.string.error_email_invalid)
                   return false
               }*/ /*else if ((phoneNumber.isEmpty() || email.isEmpty()) || (phoneNumber.isEmpty() && email.isEmpty())) {
            if (email.isEmpty() && phoneNumber.isEmpty()) {
                tilPhoneNumber.error = getString(R.string.phone_number_cant_be_empty)
                tilEmail.error = getString(R.string.email_cant_be_empty)
                return false
            } else if (!isPhoneVerified && !isEmailVerified) {
//                tilPhoneNumber.error = getString(R.string.phone_number_is_not_verified)
//                tilEmail.error = getString(R.string.email_is_not_verified)
                return false
            } else if (!isPhoneVerified && email.isEmpty()) {
                tilPhoneNumber.error = getString(R.string.phone_number_is_not_verified)
                //  tilEmail.error = getString(R.string.email_cant_be_empty)
                return false
            } else if (!isEmailVerified && phoneNumber.isEmpty()) {
                tilEmail.error = getString(R.string.email_is_not_verified)
                //tilPhoneNumber.error = getString(R.string.phone_number_cant_be_empty)
                return false
            }
        }*/


        return true
    }

    private fun onSaveClick() {
        if (validateFields())
            hitEditProfileAPI()
    }

    private fun openDatePicker(fromDOB: Boolean) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(
            this, { view, year, monthOfYear, dayOfMonth ->
                binding.etDOB.setText(formateDate("$dayOfMonth/${monthOfYear + 1}/$year"))
            },
            year,
            month,
            day
        )
        dpd.datePicker.maxDate = System.currentTimeMillis()
        dpd.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            getString(R.string.accept)
        ) { dialog, which ->
            if (fromDOB) {

                val dayOfMonth = dpd.datePicker.dayOfMonth
                val monthOfYear = dpd.datePicker.month
                val year = dpd.datePicker.year
                date = "$dayOfMonth/${monthOfYear + 1}/$year"
                binding.etDOB.setText(formateDate(date))
            } else {
                val dayOfMonth = dpd.datePicker.dayOfMonth
                val monthOfYear = dpd.datePicker.month
                val year = dpd.datePicker.year
                date = "$dayOfMonth/${monthOfYear + 1}/$year"
              //  binding.etDOD.setText(formateDate(date))

            }
        }
        dpd.setButton(
            DialogInterface.BUTTON_POSITIVE,
            getString(R.string.cancel)
        ) { dialog, which ->
            dialog.dismiss()

        }
        dpd.show()
    }

    /********************* PROFILE PICTURE **************************************/

    private fun checkStoragePermission() {

        //if (SDK_INT == Build.VERSION_CODES.Q)
//            if (!isHasPermission2(this, *permissions)){
//                askPermission(
//                    this,
//                    permissions = *permissions,
//                    requestCode = STORAGE_STORAGE_REQUEST_CODE
//                )
//
//            }else
//            optionUpload()

    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            checkPermissionFor13()
        } else {
            checkPermissionForUpto12()
        }
    }

    private fun checkPermissionFor13() {
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

    private fun checkPermissionForUpto12() {
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


    private fun optionUpload() {
        CameraGalleryDialog(this, noImage, object : CameraGalleryListener {
            override fun onCameraClicked() {
                launchCameraIntent(this@EditProfileActivity)
            }

            override fun onGalleryClicked() {
                launchGalleryIntent(this@EditProfileActivity)
            }

            override fun onRemoveImageClicked() {
                noImage = true
                Glide.with(this@EditProfileActivity).load(R.mipmap.ic_profile_pic)
                    .error(R.mipmap.ic_profile_pic)
                    .into(binding.ivProfileImage)
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
            REQUEST_IMAGE
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
            REQUEST_IMAGE
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                if (isHasPermission2(this, *permissions)) {
                    optionUpload()
                } else {
                    showDialogOK(
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

    /*************** VIEW-MODEL AND API OBSERVERS **************************************/

    private fun initViewModel() {
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        profileViewModel?.init()

        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        authViewModel?.init()
    }

    private fun hitEditProfileAPI() {
        val dob = binding.etDOB.text.toString()
        val phoneNumber = binding.etPhoneNumber.text.toString()
        val email = binding.etEmail.text.toString()
        val country = binding.etCountry.text.toString()
        val city = binding.etCity.text.toString()
        val countryCode = "+" /*+  binding.codePicker.selectedCountryCode*/
        val name =binding. etName.text.toString()

        if (!mCurrentPhotoPath.isNullOrEmpty()) {
            val file = File(mCurrentPhotoPath)
            val reqFile = file.asRequestBody("image/jpg".toMediaTypeOrNull())
            body = MultipartBody.Part.createFormData("profile_pic", file.name, reqFile)
        }

        val dobRequestBody = dob.toRequestBody()
        val genderRequestBody = gender.toRequestBody()
        val countryCodeRequestBody =
            countryCode.toRequestBody()
        val phoneNumberRequestBody =
            phoneNumber.toRequestBody()
        val emailRequestBody =
            email.toRequestBody()
        val countryRequestBody = country.toRequestBody()
        val cityRequestBody = city.toRequestBody()
        val nameRequestBody = name.toRequestBody()


        val requestPart: MutableMap<String, RequestBody> = HashMap()
        requestPart["date_of_birth"] = dobRequestBody
        requestPart["gender"] = genderRequestBody
        requestPart["country_code"] = countryCodeRequestBody
        requestPart["phone_number"] = phoneNumberRequestBody
        requestPart["email"] = emailRequestBody
        requestPart["country"] = countryRequestBody
        requestPart["city"] = cityRequestBody
        requestPart["name"] = nameRequestBody

        profileViewModel?.updateProfile(body, requestPart, this)
    }

    private fun responseData() {
        profileViewModel?.profileResponseResult?.observe(this) { profileResponse ->
            if (profileResponse != null) {
                //clParent?.showStringSnackbar(profileResponse.message ?: "")
                shortToast(profileResponse.message ?: "")
                profileResponse.data?.user?.let { userData ->
                    saveUserDetailsInPreferences(this, userData, FROM_EDIT_PROFILE)
                    val lognType = Utility.getPreferencesString(this, Constants.LOGIN_FROM)
                    if (lognType.contains("@")) {
                        Utility.savePreferencesString(
                            this@EditProfileActivity,
                            Constants.LOGIN_FROM,
                            originalEmail
                        )
                    } else {
                        Utility.savePreferencesString(
                            this@EditProfileActivity,
                            Constants.LOGIN_FROM,
                            originalPhone
                        )
                    }
                    onBackPressed()
                }
            }
        }

        profileViewModel?.verifyAuthTypeResult?.observe(
            this
        ) {
            val response = it
            if (response != null) {
                val bundle = Bundle()
                bundle.putString(Constants.FROM, Constants.FROM_AUTH_TYPE)
                bundle.putString(Constants.AUTH_TYPE, authType)
                bundle.putString(Constants.OTP, response.data?.user?.otp)
                if (authType == Constants.EMAIL) {
                    bundle.putString(Constants.EMAIL, newEmail)
                } else {
                    bundle.putString(Constants.PHONE_NUMBER, newPhone)
                    bundle.putString(Constants.COUNTRY_CODE, newCountryCode)
                }
                val intent = Intent(this@EditProfileActivity, AuthViewsHolderActivity::class.java)
                intent.putExtras(bundle)
                startActivityForResult(intent, VERIFY_AUTH_CODE)
            }
        }
    }

    private fun dataLoadingObserver() {
        profileViewModel?.updateLoaderStatus?.observe(this) {
            if (it.status) {
//                showButtonProgress()
                binding.progressLoading?.showView()
            } else {
//                revertButtonProgress()
                binding.progressLoading?.hideView()
            }
        }
        authViewModel?.updateLoaderStatus?.observe(this) {
            if (it.status) {
                showButtonProgress()
            } else {
                revertButtonProgress()
            }
        }
    }

    private fun onGettingException() {
        profileViewModel?.exception?.observe(this) {
            binding.clParent?.showStringSnackbarError(it.message)
            revertButtonProgress()
        }

        authViewModel?.exception?.observe(this) {
            binding.clParent?.showStringSnackbarError(it.message)
            revertButtonProgress()
        }
    }

    private fun onGettingError() {
        profileViewModel?.error?.observe(this) {
            binding.clParent?.showStringSnackbarError(it.message)
            revertButtonProgress()
        }
        authViewModel?.error?.observe(this) {
            binding.clParent?.showStringSnackbarError(it.message)
            revertButtonProgress()
        }
    }

    /*************************OVERRIDED METHODS ****************************************/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE -> {
                    if (resultCode == RESULT_OK) {
                        val uri = data?.getParcelableExtra<Uri>("path")
                        try {
                            uri?.let { uriFile ->
                                mCurrentPhotoPath = Utility.getRealPathFromURI(this, uriFile)
                                mCurrentPhotoPath?.let { imagePath ->
                                    val file = File(imagePath)
                                    noImage = false
                                    Glide.with(this).load(file)
                                        .error(R.mipmap.ic_profile_pic)
                                        .placeholder(R.mipmap.ic_profile_pic)
                                        .into(binding.ivProfileImage)
                                }
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
                VERIFY_AUTH_CODE -> {
                    if (authType == Constants.EMAIL) {
                        binding.verifyEmail.hideView()
                        isEmailVerified = true
                        originalEmail = binding.etEmail.text.toString().trim()
                    } else {
                        binding.verifyPhoneNumber.hideView()
                        isPhoneVerified = true
                        originalPhone = binding.etPhoneNumber.text.toString().trim()
                    }
                }

            }
        }
    }

    private fun showButtonProgress() {
        binding.btnLay?.btnContinue?.text = ""
        binding.btnLay.progressBar.showView()
        this.hideKeyboard(this)
    }

    private fun revertButtonProgress() {
        binding.btnLay?.btnContinue?.text = getString(R.string.save)
        binding.btnLay?.progressBar?.hideView()
    }

    companion object {
        fun open(currActivity: Activity) {
            val intent = Intent(currActivity, EditProfileActivity::class.java)
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left)
    }
}