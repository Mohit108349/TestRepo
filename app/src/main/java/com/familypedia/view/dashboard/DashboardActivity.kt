package com.familypedia.view.dashboard

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.familypedia.FamilyPediaApplication
import com.familypedia.R
import com.familypedia.databinding.ActivityDashboardBinding
import com.familypedia.network.DeviceInfoRequest
import com.familypedia.utils.*
import com.familypedia.utils.Constants.DEVICE_TYPE_ANDROID
import com.familypedia.utils.Constants.FROM
import com.familypedia.utils.Constants.FROM_ACCOUNT_DELETED
import com.familypedia.utils.Constants.FROM_AUTH
import com.familypedia.utils.Constants.STATUS_ACCOUNT_NOT_VERIFIED
import com.familypedia.utils.Constants.STATUS_USER_DELETED
import com.familypedia.utils.Utility.getDeviceUniqueId
import com.familypedia.view.auth.AuthViewsHolderActivity
import com.familypedia.viewmodels.AuthViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


private const val TAG = "DashboardActivity"

class DashboardActivity : AppCompatActivity(), SuccessFulListener, RestrictionListener {
    private var navController: NavController? = null
    private var authViewModel: AuthViewModel? = null
    private var from = ""
    private var email = ""
    private lateinit var binding: ActivityDashboardBinding
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            hasNotificationPermissionGranted = isGranted
            if (!isGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                            showNotificationPermissionRationale()
                        } else {
                            showSettingDialog()
                        }
                    }
                }
            } else {
                Toast.makeText(applicationContext, "notification permission granted", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private fun showSettingDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Notification Permission")
            .setMessage("Notification permission is required, Please allow notification permission from setting")
            .setPositiveButton("Ok") { _, _ ->
                val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showNotificationPermissionRationale() {

        MaterialAlertDialogBuilder(this)
            .setTitle("Alert")
            .setMessage("Notification permission is required, to show notification")
            .setPositiveButton("Ok") { _, _ ->
                if (Build.VERSION.SDK_INT >= 33) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    var hasNotificationPermissionGranted = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utility.setLocale(this)

        // Initialize View Binding
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.v("dsjfgjdfs", "" + isCallFromPost)

        initializeControl()

        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS), 9)
            }
        }

        isCallFromPost = false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Checking the request code of our request
        if (requestCode == 9) {

            // If permission is granted
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Displaying a toast
                Toast.makeText(
                    this,
                    "Permission granted",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // Displaying another toast if permission is not granted
                showSettingDialog()
            }
        }
    }


    private fun initializeControl() {
        Utility.setLocale(this)
        from = intent.extras?.getString(FROM) ?: ""
        initViewModel()
        navController()
        responseData()
        onGettingError()
        activeUser()
        onGettingException()
//        if(isCallFromPost)
//            setBottomItem(0)
    }

    private fun navController() {
        navController = Navigation.findNavController(this, R.id.home_container)
        NavigationUI.setupWithNavController(binding.bottomBar, navController!!)
        //bottomBar.setupWithNavController(navController!!)
        binding.bottomBar.setOnItemReselectedListener {
            // Do nothing to ignore the reselection
        }
        //visibilityBottomNav(navController);
        //bottomBar();
    }

    private fun showBlockedByAdminDialog() {
        SuccessfulDialog(
            this, "",
            this,
            getString(R.string.blocked),
            getString(R.string.blocked_desc)
        ).show()
    }

    private fun showAccountNotVerified(desc: String) {
        RestrictionDialog(
            this,
            this,
            getString(R.string.email_verification),
            getString(R.string.email_verification_desc)
        ).show()
    }

    override fun onUpdateSuccessfully(from: String) {
        if (from == FROM_ACCOUNT_DELETED) {
            performAccountDeleted()
        }
    }

    override fun onDismiss(from: String) {
        if (from == FROM_ACCOUNT_DELETED) {
            performAccountDeleted()
        }
    }

    override fun onOkClick() {
        finishAffinity()
    }

    private fun performAccountDeleted() {
        lifecycleScope.launch {
            Utility.clearAllPreferences(this@DashboardActivity)
                //  AuthViewsHolderActivity.open(this@DashboardActivity, null)
            finishAffinity()
        }
    }

    /*********************************************************VIEWMODELS AND API RESPONSE OBSERVERS***************************************************/

    private fun initViewModel() {
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        authViewModel?.init()
        if (from == FROM_AUTH)
            checkDeviceInfo()
    }

    private fun checkDeviceInfo() {
        val token = Utility.getPreferencesString(this, Constants.FCM_TOKEN)
        Log.e("FCM_TOKEN", "$token")
        if (token.isNotEmpty()) {
            deviceInfoAPI(token)
        } else {
            Utility.getCurrentFCMToken()
            deviceInfoAPI(token)
        }
    }

    private fun deviceInfoAPI(fcmToken: String) {
        val request = DeviceInfoRequest(
            getDeviceUniqueId(),
            DEVICE_TYPE_ANDROID,
            fcmToken,
            FamilyPediaApplication.getToken()
        )
        authViewModel?.deviceInfoAPI(request, this)
    }
    private fun activeUser(){
        val request = FamilyPediaApplication.getToken()

        authViewModel?.userActiveAPI(request,this)
    }

    private fun responseData() {
        authViewModel?.simpleResponseResult?.observe(this) { simpleResponse ->
            if (simpleResponse != null) {

            }
        }
        authViewModel?.authResponseResult?.observe(this) { simpleResponse ->
            if (simpleResponse != null) {

            }
        }
    }

    private fun onGettingException() {
        authViewModel?.exception?.observe(this) {
            binding.clParent?.showStringSnackbarError(it.message)
        }
    }

    private fun onGettingError() {
        authViewModel?.error?.observe(this) { errorData ->
            if (errorData.status == STATUS_ACCOUNT_NOT_VERIFIED) {
                Utility.unAuthorizedInactiveUser(this, errorData.message)
                return@observe
            } else if (errorData.status == STATUS_USER_DELETED) {
                //Utility.accountDeleted(this, errorData.message, lifecycleScope)
                SuccessfulDialog(
                    this, FROM_ACCOUNT_DELETED,
                    this,
                    getString(R.string.account_deleted),
                    errorData.message
                ).show()
                return@observe
            } else {
                binding.clParent?.showStringSnackbarError(errorData.message)
            }
        }
    }

    companion object {
        var isCallFromPost=false
        fun open(currActivity: Activity, bundle: Bundle) {
            val intent = Intent(currActivity, DashboardActivity::class.java)
            intent.putExtras(bundle)
            currActivity.startActivity(intent)
            currActivity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }


    public fun setBottomItem(itemAt : Int){
        binding.bottomBar.selectedItemId =  R.id.charactorsFragment
    }




}