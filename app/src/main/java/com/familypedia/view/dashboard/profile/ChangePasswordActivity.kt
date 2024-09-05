package com.familypedia.view.dashboard.profile

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.familypedia.R
import com.familypedia.databinding.ActivityChangePasswordBinding
import com.familypedia.network.*
import com.familypedia.utils.*
import com.familypedia.utils.Utility.isValidPassword
import com.familypedia.viewmodels.AuthViewModel


class ChangePasswordActivity : AppCompatActivity(), FamilyPediaClickListener, SuccessFulListener {
    private var authViewModel: AuthViewModel? = null
    private var currentPassword = ""
    private var newPassword = ""
    private var confirmNewPassword = ""
    private lateinit var binding: ActivityChangePasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeControl()
    }

    private fun initializeControl() {
        Utility.setLocale(this)
        setupViews()
        setupListeners()
        initViewModel()
        responseData()
        onGettingError()
        setClickListeners()
        dataLoadingObserver()
        onGettingException()
        binding.etCurrentPassword.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.tilCurrentPassword.isErrorEnabled = false
                binding.tilNewPassword.isErrorEnabled = false
                binding.tilConfirmNewPassword.isErrorEnabled = false
                binding.ivShowHide1.isVisible=true
                binding.ivShowHide2.isVisible=true
                binding.ivShowHide3.isVisible=true
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
        binding.etNewPassword.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.tilCurrentPassword.isErrorEnabled = false
                binding.tilNewPassword.isErrorEnabled = false
                binding.tilConfirmNewPassword.isErrorEnabled = false
                binding.ivShowHide1.isVisible=true
                binding.ivShowHide2.isVisible=true
                binding.ivShowHide3.isVisible=true
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
        binding.etConfirmNewPassword.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.tilCurrentPassword.isErrorEnabled = false
                binding.tilNewPassword.isErrorEnabled = false
                binding.tilConfirmNewPassword.isErrorEnabled = false
                binding.ivShowHide1.isVisible=true
                binding.ivShowHide2.isVisible=true
                binding.ivShowHide3.isVisible=true
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }

    private fun setupListeners() {
        binding.toolbarChangePassword.ivBack.familyPediaClickListener(this)
        binding.btnLay?.btnContinue?.familyPediaClickListener(this)
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding.toolbarChangePassword?.ivBack -> onBackPressed()
            binding.btnLay?.btnContinue -> hitChangePasswordAPI()
        }
    }

    private fun setupViews() {
        binding.toolbarChangePassword.tvToolbarTitle.text = getString(R.string.change_password)
        binding.btnLay?.btnContinue?.text=getString(R.string.submit)
    }

    private fun validateFields(): Boolean {
        currentPassword = binding.etCurrentPassword.text.toString()
        newPassword = binding.etNewPassword.text.toString()
        confirmNewPassword = binding.etConfirmNewPassword.text.toString()
        removeErrors()
        if (currentPassword.isEmpty()) {
            binding.tilCurrentPassword.error = getString(R.string.error_password_empty)
            binding.ivShowHide1.isVisible=false
            return false
        } else if (!isValidPassword(currentPassword)) {
            binding.tilCurrentPassword.error = getString(R.string.error_password_invalid)
            binding.ivShowHide1.isVisible=false
            return false
        } else if (newPassword.isEmpty()) {
            binding.tilNewPassword.error = getString(R.string.error_new_password_empty)
            binding.ivShowHide2.isVisible=false
            return false
        } else if (!isValidPassword(newPassword)) {
            binding.tilNewPassword.error = getString(R.string.error_password_invalid)
            binding.ivShowHide2.isVisible=false
            return false
        } else if (confirmNewPassword.isEmpty()) {
            binding.tilConfirmNewPassword.error = getString(R.string.error_confirm_password_empty)
            binding.ivShowHide3.isVisible=false
            return false
        } else if (confirmNewPassword != newPassword) {
            binding.tilConfirmNewPassword.error = getString(R.string.error_confirm_password_unmatch)
            binding.ivShowHide3.isVisible=false
            return false
        }
        return true
    }



    private fun removeErrors() {
        binding.tilCurrentPassword.isErrorEnabled = false
        binding.tilNewPassword.isErrorEnabled = false
        binding.tilConfirmNewPassword.isErrorEnabled = false
    }

    private fun dialogPasswordChanged() {
        SuccessfulDialog(
            this, "",
            this,
            getString(R.string.password_changed),
            getString(R.string.password_changed_successfully)
        ).show()
    }

    override fun onUpdateSuccessfully(from:String) {
        onBackPressed()
    }

    override fun onDismiss(from:String) {

    }

    /*********************************************************VIEW-MODELS AND API RESPONSE OBSERVERS***************************************************/

    private fun initViewModel() {
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        authViewModel?.init()
    }

    private fun hitChangePasswordAPI() {
        if (validateFields()) {
            this.hideKeyboard(this)
            val changePasswordRequest = ChangePasswordRequest(currentPassword, newPassword)
            authViewModel?.changePasswordAPI(changePasswordRequest, this)
        }
    }

    private fun responseData() {
        authViewModel?.simpleResponseResult?.observe(this) { simpleResponse ->
            if (simpleResponse != null) {
                dialogPasswordChanged()
            }
        }
    }

    private fun dataLoadingObserver() {
        authViewModel?.updateLoaderStatus?.observe(this, {
            if (it.status) {
                showButtonProgress()
            } else {
                revertButtonProgress()
            }
        })
    }

    private fun onGettingException() {
        authViewModel?.exception?.observe(this, {
            binding.clParent?.showStringSnackbarError(it.message)
            revertButtonProgress()
        })
    }

    private fun onGettingError() {
        authViewModel?.error?.observe(this, {
            binding.clParent?.showStringSnackbarError(it.message)
            revertButtonProgress()
        })
    }

    private fun showButtonProgress() {
        binding.btnLay?.btnContinue?.text = ""
        binding.btnLay?.progressBar?.showView()
    }

    private fun revertButtonProgress() {
        binding.btnLay?.btnContinue?.text = getString(R.string.submit)
        binding.btnLay?.progressBar?.hideView()
    }

    companion object {
        fun open(currActivity: Activity) {
            val intent = Intent(currActivity, ChangePasswordActivity::class.java)
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        this.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left)
    }

    private fun setClickListeners() {
        var isVisible = false

        binding.ivShowHide1.setOnClickListener {
            if (isVisible) {
                binding.etCurrentPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                isVisible = false
                binding.ivShowHide1.setBackgroundResource(R.drawable.password_visible)
            } else {
                binding.etCurrentPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                isVisible = true
                binding.ivShowHide1.setBackgroundResource(R.drawable.password_invisible)
            }
        }
        binding.ivShowHide2.setOnClickListener {
            if (isVisible) {
                binding.etNewPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                isVisible = false
                binding.ivShowHide2.setBackgroundResource(R.drawable.password_visible)
            } else {
                binding.etNewPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                isVisible = true
                binding.ivShowHide2.setBackgroundResource(R.drawable.password_invisible)
            }
        }
        binding.ivShowHide3.setOnClickListener {
            if (isVisible) {
                binding.etConfirmNewPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                isVisible = false
                binding.ivShowHide3.setBackgroundResource(R.drawable.password_visible)
            } else {
                binding.etConfirmNewPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                isVisible = true
                binding.ivShowHide3.setBackgroundResource(R.drawable.password_invisible)
            }
        }

//        tvForgotPassword.familyPediaClickListener(this)
//        btnContinue.familyPediaClickListener(this)
    }
}