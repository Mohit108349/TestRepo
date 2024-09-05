package com.familypedia

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.familypedia.databinding.ActivityDeleteAccountScreenBinding
import com.familypedia.network.DELETE_USER
import com.familypedia.network.VerifyPassword
import com.familypedia.utils.*
import com.familypedia.view.auth.AuthViewsHolderActivity
import com.familypedia.viewmodels.AuthViewModel

class DeleteAccountScreenActivity : AppCompatActivity(), FamilyPediaClickListener,
    ConfirmationDeleteDialogListener, DeleteSuccessDialogListener {
    private var authViewModel: AuthViewModel? = null
    private var deleteAccountException: DeleteAccounException? = null
    private lateinit var binding: ActivityDeleteAccountScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityDeleteAccountScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set text using binding
        binding.btnLay.btnContinue.text = resources.getString(R.string.tv_delete_accounts)
        binding.toolbarChangePassword.tvToolbarTitle.text = resources.getString(R.string.tv_delete_account)

        initializeControl()
    }

    private fun initializeControl() {
        binding.etCurrentPassword.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        Utility.setLocale(this)
        setupListeners()
        initViewModel()
        responseData()
        onGettingError()
        dataLoadingObserver()
        onGettingException()
    }

    private fun showButtonProgress() {
        binding.btnLay.btnContinue.text = ""
        binding.btnLay.progressBar.showView()
    }

    private fun dataLoadingObserver() {
        authViewModel?.updateLoaderStatus?.observe(this) {
            if (it.status) {
                showButtonProgress()
            } else {
                revertButtonProgress()
            }
        }
    }

    private fun revertButtonProgress() {
        binding.btnLay.btnContinue?.text = getString(R.string.tv_delete_accounts)
        binding.btnLay.progressBar?.hideView()
    }


    /*********************************************************VIEW-MODELS AND API RESPONSE OBSERVERS***************************************************/

    private fun initViewModel() {
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        authViewModel?.init()
    }

    private fun responseData() {
        authViewModel?.simpleResponseResult?.observe(this) { simpleResponse ->
            if (simpleResponse != null) {
                if (simpleResponse.apiName == DELETE_USER) {
                    binding.clParent?.showStringSnackbar(simpleResponse.message ?: "")
                    Utility.clearAllPreferences(this)
                    deleteSuccess(simpleResponse.message ?: "")
                } else {
                    binding. clParent?.showStringSnackbar(simpleResponse.message ?: "")
                    showDeleteDialog()
                }

            }
        }

    }


    private fun onGettingException() {
        authViewModel?.exception?.observe(this) {
            if (it.apiName == DELETE_USER) {
                //clParent?.showStringSnackbarError(it.message)
                deleteAccountException?.onExceptionOccured()
                Utility.clearAllPreferences(this)
                deleteSuccess(null)
            } else {
                binding. clParent?.showStringSnackbarError(it.message)
            }
        }
    }

    private fun onGettingError() {
        authViewModel?.error?.observe(this) {
            if (it.apiName == DELETE_USER) {
                //clParent?.showStringSnackbarError(it.message)
                deleteAccountException?.onExceptionOccured()
            } else {
                binding. clParent?.showStringSnackbarError(it.message)
            }
        }
    }

    private fun setupListeners() {
        binding.toolbarChangePassword.ivBack.familyPediaClickListener(this)
        binding.btnLay.btnContinue.familyPediaClickListener(this)
    }

    private fun onSaveClick() {
        if (validateFields()) {
            hideKeyboard(this)
            callPasswordVerificationApi()
        }
    }

    private fun callPasswordVerificationApi() {
        val req = VerifyPassword(binding.etCurrentPassword.text.toString().trim())
        authViewModel?.verifyPassword(this, req)
    }

    private fun validateFields(): Boolean {
        removeErrors()
        if (binding.etCurrentPassword.text.toString().isEmpty()) {
            binding.tilPassword.error = resources.getString(R.string.error_password_empty)
            return false
        }
        return true
    }

    private fun removeErrors() {
        binding.tilPassword.isErrorEnabled = false
    }

    private var confirmationDialog: DeleteAccountDialog? = null
    private fun showDeleteDialog() {
        confirmationDialog = DeleteAccountDialog(
            this,
            this,
            getString(R.string.tv_account_delete_popup),

            )
        confirmationDialog?.show()
    }

    companion object {
        fun open(currActivity: Activity) {
            val intent = Intent(currActivity, DeleteAccountScreenActivity::class.java)
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_left)
    }

    override fun onViewClick(view: View) {
        when (view) {
            binding.toolbarChangePassword.ivBack -> onBackPressed()
            binding.btnLay.btnContinue -> onSaveClick()

        }
    }

    override fun onYes(exception: DeleteAccounException) {
        deleteAccountException = exception
        deleteUser(this)
    }

    override fun onNo() {

    }

    private fun deleteUser(context: Context) {
        authViewModel?.deleteUserAccount(context, FamilyPediaApplication.getUserId())
    }

    private fun deleteSuccess(msg: String?) {
        var confirmationDialog: DeleteSuccessDialog? = null
        confirmationDialog = DeleteSuccessDialog(
            this,
            this,
            msg ?: getString(R.string.account_deleted)
        )
        confirmationDialog.show()
    }

    override fun onOK() {
        AuthViewsHolderActivity.open(this, null)
        finishAffinity()
    }
}