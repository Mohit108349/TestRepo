package com.familypedia.view.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import com.familypedia.R
import com.familypedia.databinding.ActivityAuthViewsHolderBinding
import com.familypedia.network.*
import com.familypedia.utils.*
import com.familypedia.utils.Constants.FROM
import com.familypedia.utils.Constants.FROM_AUTH
import com.familypedia.utils.Constants.FROM_BIO_LINK
import com.familypedia.utils.Constants.FROM_FORGOT_PASSWORD
import com.familypedia.utils.Constants.FROM_PHONE
import com.familypedia.utils.Constants.FROM_SIGNUP
import com.familypedia.utils.Constants.FROM_SIGNUP_CONFLICT
import com.familypedia.utils.Constants.LOGIN_FROM
import com.familypedia.view.auth.phone_login.SignUpWithPhoneFragmentDirections
import com.familypedia.view.dashboard.DashboardActivity
import com.familypedia.viewmodels.AuthViewModel

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


interface OnClickCallback {
    fun onLoginButtonClick(loginRequest: LoginRequest, progressButton: View, buttonText: String)
    fun onSignupClick()
    fun onForgotPasswordClick()
    fun onSignupButtonClick(signupRequest: SignupRequestNew, buttonText: String)
    fun onSendPasswordResetButtonClick(emailRequest: ForgotPasswordRequest, buttonText: String)
    fun onResendButtonClick(emailRequest: ForgotPasswordRequest)
    fun onResendOtpButtonClick(request: ResendOtpRequest, comeFrom: String)
    fun onVerifyButtonClick(verifyOTP: VerifyOTPRequest, from: Int)
}

class AuthViewsHolderActivity : AppCompatActivity(), OnClickCallback, FamilyPediaClickListener,
    SuccessFulListener, AuthDialogListener {
    var currentFragment: Fragment? = null
    var navHostFragment: Fragment? = null
    private var controller: NavController? = null
    private var authViewModel: AuthViewModel? = null
    private var buttonText: String = ""
    private var verificationEmail = ""
    private var forgotPasswordEmailRequest: ForgotPasswordRequest? = null
    private var signupRequest: SignupRequestNew? = null
    private var invitedUserId = ""
    private var signUpType = ""
    private var isFromProfile = false
    private var comeFrom = ""
    private var authWith = 0
    private var otpForgotPass = ""
    private var from = ""
    private var characterId = ""
    private lateinit var binding: ActivityAuthViewsHolderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthViewsHolderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeFlow()
    }

    private fun initializeFlow() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        controller = navHostFragment?.findNavController()

        setClickListeners()
        initViewModel()
        responseData()
        onGettingError()
        onGettingException()
        getIntentData()
        Utility.getCurrentFCMToken()

    }

    private fun getIntentData() {
        verificationEmail = intent.extras?.getString(Constants.EMAIL) ?: ""
        invitedUserId = intent.extras?.getString(Constants.USER_ID) ?: ""
        from = intent.extras?.getString(Constants.FROM) ?: ""
        if (from.isEmpty()) {
            if (verificationEmail.isNotEmpty()) {
                hitVerifyAccountApi(verificationEmail)
            }
        } else {

            val bundle = intent?.extras
            if (bundle != null) {
                navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
                controller = navHostFragment?.findNavController()

                if (from == Constants.FROM_AUTH_TYPE) {
                    val email = bundle.getString(Constants.EMAIL) ?: ""
                    val phone = bundle.getString(Constants.PHONE_NUMBER) ?: ""
                    val cc = bundle.getString(Constants.COUNTRY_CODE) ?: ""
                    val authType = bundle.getString(Constants.AUTH_TYPE) ?: ""
                    val otp = bundle.getString(Constants.OTP) ?: ""

                    if (authType == Constants.EMAIL) {
                        isFromProfile = true
                        val bundleOtp = Bundle()
                        bundleOtp.putInt("comeFrom", Constants.FROM_EMAIL)
                        bundleOtp.putString("id", email)
                        bundleOtp.putString(Constants.OTP, otp)
                        controller?.navigate(R.id.verificationFragment, bundleOtp)
                    } else {
                        val bundleOtp = Bundle()
                        bundleOtp.putInt("comeFrom", Constants.FROM_PHONE)
                        bundleOtp.putString("id", phone)
                        bundleOtp.putString("cc", cc)
                        bundleOtp.putString(Constants.OTP, otp)
                        controller?.navigate(R.id.verificationFragment, bundleOtp)
                    }
                }
                if (from == FROM_BIO_LINK) {
                    characterId = intent?.extras?.getString(Constants.CHARACTER_ID) ?: ""
                }
            }
        }
    }

    override fun onSignupClick() {
        Navigation.findNavController(binding.fragmentContainer)
            .navigate(R.id.actionOpenSignup)
    }

    override fun onForgotPasswordClick() {
        forgotPasswordNavigation()
    }

    private fun forgotPasswordNavigation() {
//        Navigation.findNavController(fragment_container)
//            .navigate(R.id.actionOpenForgotPassword)
    }

    private fun verificationCodeNavigation(response: AuthResponseModel) {
//        controller?.navigate(R.id.verificationFragment)
//        /*NavController(this).navigate(R.id.verificationFragment,Bundle())*/
///*
//        controller?.navigate(R.id.action_signupWithEmailFragment_to_signUpWithPhoneFragment)
//*/

        val bundle = Bundle()
        val value = response.value
        bundle.putString("id", value)
        if (value?.contains("@") == true) {
            bundle.putInt("comeFrom", Constants.FROM_EMAIL)
            bundle.putString("id", value)
            Navigation.findNavController(binding.fragmentContainer)
                .navigate(R.id.verificationFragment, bundle)
        } else if (value == null && response.data?.user?.email != null) {
            bundle.putInt("comeFrom", Constants.FROM_EMAIL)
            bundle.putString("id", response.data.user.email)
            bundle.putString(Constants.OTP, response.data.user.otp)
            bundle.putString(Constants.FROM, Constants.FROM_FORGOT_PASS)
            Navigation.findNavController(binding.fragmentContainer)
                .navigate(R.id.verificationFragment, bundle)
        } else if (value == null && response.data?.user?.phone_number != null) {
            bundle.putInt("comeFrom", Constants.FROM_PHONE)
            bundle.putString("id", response.data.user.phone_number.toString())
            bundle.putString("cc", response.data.user.country_code.toString())
            bundle.putString(Constants.OTP, response.data.user.otp)
            bundle.putString(Constants.FROM, Constants.FROM_FORGOT_PASS)
            Navigation.findNavController(binding.fragmentContainer)
                .navigate(R.id.verificationFragment, bundle)
        } else {
            bundle.putInt("comeFrom", Constants.FROM_PHONE)
            bundle.putString("id", response.data?.user?.phone_number.toString())
            bundle.putString("cc", response.data?.user?.country_code.toString())
            Navigation.findNavController(binding.fragmentContainer)
                .navigate(R.id.verificationFragment, bundle)
            Log.e("after_nav", "verificationCodeNavigation: ")
        }


    }

    override fun onSignupButtonClick(signupRequest: SignupRequestNew, buttonText: String) {
        if (signupRequest.email != null) {
            signUpType = Constants.EMAIL
            Utility.savePreferencesString(this, Constants.LOGIN_FROM, signupRequest.email)
        } else {
            signUpType = Constants.PHONE_NUMBER
            signupRequest.phoneNumber?.let {
                Utility.savePreferencesString(
                    this, Constants.LOGIN_FROM,
                    it
                )
            }
        }
        this.signupRequest = signupRequest
        showButtonProgress(buttonText)
        hitSignupAPI(signupRequest)
    }


    override fun onSendPasswordResetButtonClick(
        emailRequest: ForgotPasswordRequest,
        buttonText: String,
    ) {
        forgotPasswordEmailRequest = emailRequest
        showButtonProgress(buttonText)
        //createDynamicLink(emailRequest)
        hitForgotPasswordAPI(emailRequest)

    }

    override fun onResendButtonClick(emailRequest: ForgotPasswordRequest) {
        hitResendForgotPasswordOtpAPI(emailRequest)
    }

    override fun onResendOtpButtonClick(request: ResendOtpRequest, comefrom: String) {
        comeFrom = comefrom
        hitResendSignUpOtpAPI(request)
    }

    override fun onVerifyButtonClick(verifyOTP: VerifyOTPRequest, from: Int) {
        hitVerifyOtpAPI(otpRequest = verifyOTP)
        otpForgotPass = verifyOTP.otp.toString()
        authWith = from
    }

    override fun onLoginButtonClick(
        loginRequest: LoginRequest,
        progressButton: View,
        buttonText: String,
    ) {
//        if (loginRequest.email != null) {
//            Utility.savePreferencesString(this, Constants.LOGIN_FROM, loginRequest.email)
//        } else {
//            loginRequest.phoneNumber?.let {
//                Utility.savePreferencesString(this, Constants.LOGIN_FROM,it)
//            }
//        }
        showButtonProgress(buttonText)
        hitLoginAPI(loginRequest)
    }

    override fun onUpdateSuccessfully(from: String) {
        // signup updatesucess means,, clicked on resend link


    }

    override fun onDismiss(from: String) {

    }

    //
    override fun onAuthDialogDismiss(from: String) {
    }

    override fun onAuthDialogResendLinkClick(from: String) {
        if (from == FROM_SIGNUP) {
            //hit resend link
            resendVerificationLink()
        } else if (from == FROM_SIGNUP_CONFLICT) {
            //send to forgot password
            forgotPasswordNavigation()
        } else if (from == FROM_FORGOT_PASSWORD) {
            //hit api for resend
            forgotPasswordEmailRequest?.let { request ->
                hitResendForgotPasswordOtpAPI(request)
            }
        }
    }


    private fun setClickListeners() {
        binding.ivBack.familyPediaClickListener(this)
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding.ivBack -> {
                if (!isFromProfile) {
                    onBackPressed()
                } else finish()
            }
        }
    }

    private val listener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            manageFragmentStatesAndStepView(destination)
        }


    private fun manageFragmentStatesAndStepView(destination: NavDestination) {
        currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)
        when (destination.id) {
            R.id.loginFragment ->
                manageBackButton(false)
            R.id.forgotPasswordFragment ->
                manageBackButton(true)
            R.id.signupWithEmailFragment ->
                manageBackButton(true)
            R.id.authLoginTypeFragment ->
                manageBackButton(false)

        }
    }

    private fun manageBackButton(show: Boolean) {
        if (show)
            binding.ivBack.showView()
        else
            binding.ivBack.hideView()
    }

    override fun onResume() {
        super.onResume()
        controller?.addOnDestinationChangedListener(listener)
    }

    override fun onPause() {
        controller?.removeOnDestinationChangedListener(listener)
        super.onPause()
    }

    private fun showButtonProgress(text: String) {
        this.buttonText = text
        //btnContinue.text = ""
        //progressBar.showView()
        this.hideKeyboard(this)
    }

    private fun revertButtonProgress() {
        //btnContinue.isClickable = true
        //btnContinue?.text = buttonText
        //progressBar?.hideView()
    }

    private fun accountVerifiedDialog(response: AuthResponseModel) {
        var verifcationDialog: SuccessfulDialog? = null
        if (authWith == FROM_PHONE) {
            verifcationDialog = SuccessfulDialog(
                this, "",
                this,
                getString(R.string.phone_number_verified),
                response.message ?: ""
            )
        } else {
            verifcationDialog = SuccessfulDialog(
                this, "",
                this,
                getString(R.string.email_verification),
                response.message ?: ""
            )
        }

        verifcationDialog.show()
        lifecycleScope.launch {
            delay(3000)
            verifcationDialog.dismiss()
            if (comeFrom == Constants.FROM_FORGOT_PASS) {
                response.data?.user?.let { navigateResetPassword(it) }
            } else {
                response.data?.user?.let { navigateToDash(it) }

            }


            /*      if (comeFrom == Constants.FROM_RESEND_OTP) {
                      response.data?.user?.let { navigateToDash(it) }
                  } else if (comeFrom == Constants.FROM_FORGOT_PASS){
                      response.data?.user?.let { navigateResetPassword(it) }
                  }*/
        }
    }

    private fun openAuthDialog(from: String, title: String, desc: String) {
        AuthDialog(
            this, from,
            this,
            title,
            desc
        ).show()

    }

    private fun phoneVerifiedDailog() {
        PhoneNumberVerified(
            this, "",
            object : SuccessFulListener {
                override fun onUpdateSuccessfully(from: String) {
                }

                override fun onDismiss(from: String) {
                }
            },
        ).show()
    }

    /*********************************************************VIEWMODELS AND API RESPONSE OBSERVERS***************************************************/

    private fun initViewModel() {
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        authViewModel?.init()
    }

    private fun hitSignupAPI(signupRequest: SignupRequestNew) {
        authViewModel?.signupToTheApplication(this, signupRequest)
    }

    private fun hitLoginAPI(loginRequest: LoginRequest) {
        authViewModel?.loginToTheApplication(this, loginRequest)
    }

    private fun hitForgotPasswordAPI(request: ForgotPasswordRequest) {
        authViewModel?.forgotPassword(this, request)
    }

    private fun hitResendForgotPasswordOtpAPI(emailRequest: ForgotPasswordRequest) {
//        authViewModel?.resendOtpAPI(this, emailRequest)
    }

    private fun hitResendSignUpOtpAPI(resendRequest: ResendOtpRequest) {
        authViewModel?.resendOtpAPI(this, resendRequest)
    }

    private fun hitVerifyOtpAPI(otpRequest: VerifyOTPRequest) {
        authViewModel?.verifyOtpAPI(this, otpRequest)
    }

    private fun hitVerifyAccountApi(email: String) {
        authViewModel?.verifyAccount(EmailRequest(email), this)
    }

    private fun resendVerificationLink() {
        /*  signupRequest?.let { signupRequest ->
              val resendSignupLinkRequest =
                  ResendSignupLinkRequest(signupRequest.dynamicLink, signupRequest.email)
              authViewModel?.resendSignupVerificationLink(resendSignupLinkRequest, this)
          }*/
    }

    private fun responseData() {
        authViewModel?.authResponseResult?.observe(this) { authResponse ->
            if (authResponse.apiName != VERIFY_ACCOUNT)
                revertButtonProgress()
            if (authResponse != null) {
                if (authResponse.apiName == SIGNUP) {
                    verificationCodeNavigation(authResponse)

                    /*              openAuthDialog(
                                      FROM_SIGNUP,
                                      getString(R.string.email_verification),
                                      getString(R.string.email_verification_desc)
                                  )*/
                }
                if (authResponse.apiName == FORGOT_PASSWORD) {
                    openAuthDialog(
                        FROM_FORGOT_PASSWORD,
                        getString(R.string.password_link_sent),
                        getString(R.string.dialog_desc_signup)
                    )
                }

                if (authResponse.apiName == LOGIN) {
                    authResponse.data?.user?.let { user ->
                        if (false) {
                            onBackPressed()
                        } else {
                            navigateToDash(user)
                        }
                    }
                }
                if (authResponse.apiName == RESEND_OTP) {
                    if (comeFrom == Constants.FROM_FORGOT_PASSWORD) {
                        verificationCodeNavigation(authResponse)
                    } else shortToast(authResponse.data?.user?.otp ?: "")
                }

                if (authResponse.apiName == VERIFY_OTP)
                    accountVerifiedDialog(authResponse)
                if (authResponse.apiName == VERIFY_ACCOUNT)
                    accountVerifiedDialog(authResponse)
            }
        }

        authViewModel?.simpleResponseResult?.observe(this) { simpleResponseModel ->
            if (simpleResponseModel != null) {
                if (simpleResponseModel.apiName == RESEND_SIGNUP) {
                    binding.clParent?.showStringSnackbar(simpleResponseModel.message ?: "")
                }
            }
        }
    }

    private fun onGettingException() {
        authViewModel?.exception?.observe(this) { exceptionData ->
            binding.clParent?.showStringSnackbarError(exceptionData.message)
            if (exceptionData.apiName != RESEND_SIGNUP) {
                revertButtonProgress()
            }
        }
    }

    private fun onGettingError() {
        authViewModel?.error?.observe(this) { errorData ->
            binding.clParent?.showStringSnackbarError(errorData.message)
            if (errorData.apiName != RESEND_SIGNUP) {
                revertButtonProgress()
                if (errorData.apiName == SIGNUP) {
                    //onBackPressed()
                    if (signUpType == Constants.EMAIL) {
                        openAuthDialog(
                            FROM_SIGNUP_CONFLICT,
                            getString(R.string.email_already_registered),
                            getString(R.string.your_email_already_registered)
                        )
                    } else {
                        openAuthDialog(
                            FROM_SIGNUP_CONFLICT,
                            getString(R.string.phone_number_already_registered),
                            getString(R.string.your_phone_number_already_registered)
                        )
                    }
                } else if (errorData.apiName == RESEND_OTP) {
                  //  btnContinue.text = resources.getText(R.string.login)
                }
            }
        }
    }

    private fun navigateToDash(user: User) {
        Utility.saveUserDetailsInPreferences(this, user, FROM_AUTH)
        val loginFromValue = if (user.account_type == "EMAIL") user.email ?: "" else user.phone_number ?: ""
        Utility.savePreferencesString(this, loginFromValue, LOGIN_FROM)
        val bundle = Bundle()
        bundle.putString(FROM, FROM_AUTH)
        DashboardActivity.open(this, bundle)
        finishAffinity()
    }

    private fun navigateResetPassword(user: User) {
        val bundle = Bundle()
        bundle.putString(FROM, FROM_FORGOT_PASSWORD)
        bundle.putString(FROM, FROM_FORGOT_PASSWORD)
        ResetPasswordActivity.open(this, bundle)
        finishAffinity()
    }

    companion object {
        fun open(currActivity: Activity, bundle: Bundle?) {
            val intent = Intent(currActivity, AuthViewsHolderActivity::class.java)
            bundle?.let {
                intent.putExtras(bundle)
            }
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }
}