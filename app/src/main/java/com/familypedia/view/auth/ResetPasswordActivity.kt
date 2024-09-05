package com.familypedia.view.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.familypedia.R
import com.familypedia.databinding.ActivityResetPasswordBinding
import com.familypedia.network.ForgotPasswordRequest
import com.familypedia.utils.*
import com.familypedia.utils.Constants.EMAIL
import com.familypedia.utils.Constants.FROM_EMAIL
import com.familypedia.utils.Utility.isValidPassword
import com.familypedia.viewmodels.AuthViewModel
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "ResetPasswordActivity"

class ResetPasswordActivity : AppCompatActivity(), FamilyPediaClickListener,SuccessFulListener {
    private var authViewModel: AuthViewModel? = null
    private var password = ""
    private var confirmNewPassword = ""
    var email = ""
    var from = 0
    var phone = ""
    var cc = ""
    var otp = ""
    var resetDialog:ResetPasswordDialog? = null
    private lateinit var binding: ActivityResetPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeControl()
    }

    private fun initializeControl() {
        Utility.setLocale(this)
        binding.layBtn.btnContinue.text = getString(R.string.reset_password)
//        email=intent.extras?.getString(EMAIL)?:return
        getIntentData()
        setViews()
        setClickListener()
        //acceptDynamicLink()
        initViewModel()
        responseData()
        onGettingError()
        dataLoadingObserver()
        onGettingException()

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.tilPassword.isErrorEnabled = false
                binding.tilConfirmPassword.isErrorEnabled = false
                binding.ivShowConformPass.isVisible=true
                binding.ivShowHideNormal.isVisible=true
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.tilPassword.isErrorEnabled = false
                binding.tilConfirmPassword.isErrorEnabled = false
                binding.ivShowConformPass.isVisible=true
                binding.ivShowHideNormal.isVisible=true
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

    }

    private fun getIntentData(){
        intent?.extras?.let {
            from = it.getInt(Constants.FROM)
            email = it.getString(Constants.EMAIL)?: ""
          phone = it.getString(Constants.PHONE_NUMBER, "")
            otp = it.getString(Constants.OTP, "")
            cc = it.getString(Constants.COUNTRY_CODE, "")
        }

    }
    private fun setViews(){
        binding.toolbarResetPassword.tvToolbarTitle.setText(R.string.reset_password)
    }

    private fun setClickListener() {
        var isVisible = false
        binding.layBtn.btnContinue.familyPediaClickListener(this)
        binding.toolbarResetPassword.ivBack.familyPediaClickListener(this)

        binding.ivShowHideNormal.setOnClickListener {
            if (isVisible) {
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                isVisible = false
                binding.ivShowHideNormal.setBackgroundResource(R.drawable.password_visible)
            } else {
                binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                isVisible = true
                binding.ivShowHideNormal.setBackgroundResource(R.drawable.password_invisible)
            }
        }
        binding.ivShowConformPass.setOnClickListener {
            if (isVisible) {
                binding.etConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                isVisible = false
                binding.ivShowConformPass.setBackgroundResource(R.drawable.password_visible)
            } else {
                binding.etConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                isVisible = true
                binding.ivShowConformPass.setBackgroundResource(R.drawable.password_invisible)
            }
        }
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding.layBtn.btnContinue -> hitResetPassword()
            binding.toolbarResetPassword.ivBack -> onBackPressed()
        }
    }

    private fun acceptDynamicLink() {
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    email = deepLink?.getQueryParameter("email") ?: ""
                    Log.d(TAG, "initializeControl: $email")
                    // tvSetNewPassword.text=getString(R.string.set_your_new_password_below)+" for "+email
                }

                // Handle the deep link. For example, open the linked
                // content, or apply promotional credit to the user's
                // account.
                // ...

                // ...
            }
            .addOnFailureListener(this) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }
    }

    private fun validateFields(): Boolean {
        binding.tilPassword.isErrorEnabled=false
        binding.tilConfirmPassword.isErrorEnabled=false
        password=binding.etPassword.text.toString()
        confirmNewPassword=binding.etConfirmPassword.text.toString()
        if (password.isEmpty()){
            binding.tilPassword.error=getString(R.string.enter_password)
            binding.ivShowHideNormal.isVisible=false
            return false
        }else if (!isValidPassword(password)){
            binding.tilPassword.error=getString(R.string.error_password_invalid)
            binding.ivShowHideNormal.isVisible=false
            return false
        }else if (confirmNewPassword.isEmpty()){
            binding.tilConfirmPassword.error=getString(R.string.error_confirm_password_empty)
            binding.ivShowConformPass.isVisible=false
            return false
        }else if (confirmNewPassword!=password){
            binding.tilConfirmPassword.error=getString(R.string.error_confirm_password_unmatch)
            binding.ivShowConformPass.isVisible=false
            return false
        }

        return true
    }

    private fun showSuccessDialog(){
       resetDialog = ResetPasswordDialog(
            this, "",
            this,
            getString(R.string.password_changed_successfully)
        )
        resetDialog?.show()
        lifecycleScope.launch {
            delay(5000)
            resetDialog?.dismiss()
            finishAffinity()
            AuthViewsHolderActivity.open(this@ResetPasswordActivity,null)
        }
    }

    override fun onUpdateSuccessfully(from:String) {
        finishAffinity()
        resetDialog?.dismiss()
        AuthViewsHolderActivity.open(this@ResetPasswordActivity,null)
    }

    override fun onDismiss(from:String) {

    }
    /*********************************************************VIEW-MODELS AND API RESPONSE OBSERVERS***************************************************/

    private fun initViewModel() {
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        authViewModel?.init()
    }

    private fun hitResetPassword() {
        if (from == FROM_EMAIL) {
            if (validateFields()) {
                val changePasswordRequest = ForgotPasswordRequest(email, null, null, otp, password)
                authViewModel?.resetPasswordAPI(changePasswordRequest, this)
            }
        } else
            if (validateFields()) {
                val changePasswordRequest = ForgotPasswordRequest(null, phone, cc, otp, password)
                authViewModel?.resetPasswordAPI(changePasswordRequest, this)
            }
    }

    private fun responseData() {
        authViewModel?.simpleResponseResult?.observe(this) { simpleResponse ->
            if (simpleResponse != null) {
                showSuccessDialog()
                //dialogPasswordChanged()
            }
        }
    }

    private fun dataLoadingObserver() {
        authViewModel?.updateLoaderStatus?.observe(this) {
            if (it.status!!) {
                showButtonProgress()
            } else {
                revertButtonProgress()
            }
        }
    }

    private fun onGettingException() {
        authViewModel?.exception?.observe(this) {
            binding.clParent?.showStringSnackbarError(it.message)
            Log.e(TAG, "onGettingException: ${it.message}" )
            revertButtonProgress()
        }
    }

    private fun onGettingError() {
        authViewModel?.error?.observe(this) {
            binding.clParent?.showStringSnackbarError(it.message)
            revertButtonProgress()
            Log.e(TAG, "onGettingError: ${it.message}" )
        }
    }

    private fun showButtonProgress() {
        binding.layBtn.btnContinue.text = ""
        binding.layBtn.progressBar.showView()
    }

    private fun revertButtonProgress() {
       binding.layBtn. btnContinue?.text = getString(R.string.reset_password)
        binding.layBtn.progressBar?.hideView()
    }


    companion object {
        fun open(currActivity: Activity, bundle: Bundle) {
            val intent = Intent(currActivity, ResetPasswordActivity::class.java)
            intent.putExtras(bundle)
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }
}