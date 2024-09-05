package com.familypedia

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf
import com.familypedia.network.CharacterData
import com.familypedia.network.User
import com.familypedia.network.UserListResponseModel
import com.familypedia.utils.Constants
import com.familypedia.utils.Constants.CHARACTER_ID
import com.familypedia.utils.Constants.DYNAMIC_LINK
import com.familypedia.utils.Constants.EMAIL
import com.familypedia.utils.Constants.FROM
import com.familypedia.utils.Constants.FROM_BIO_LINK
import com.familypedia.utils.Constants.FROM_INVITATION_LINK
import com.familypedia.utils.Constants.FROM_SPLASH
import com.familypedia.utils.Constants.FROM_VERIFY_ACCOUNT
import com.familypedia.utils.Constants.NOTIFICATION
import com.familypedia.utils.Constants.NOTIFICATION_TYPE
import com.familypedia.utils.Constants.USER_ID
import com.familypedia.utils.Utility
import com.familypedia.utils.Utility.getPreferencesBoolean
import com.familypedia.utils.Utility.isLogin
import com.familypedia.utils.toast
import com.familypedia.view.auth.AuthViewsHolderActivity
import com.familypedia.view.auth.ResetPasswordActivity
import com.familypedia.view.dashboard.DashboardActivity
import com.familypedia.view.dashboard.character.aboutCharacters.AboutCharacterActivity
import com.familypedia.view.dashboard.character.addCharacter.AddCharacterListingActivity
import com.familypedia.view.dashboard.character.post.AddNewPostActivity
import com.familypedia.view.notifications.NotificationsActivity
import com.familypedia.walkthrough.WalkThroughActivity
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

private const val TAG = "SplashActivity"

class SplashActivity : AppCompatActivity()/*, OnSuccessListener<PendingDynamicLinkData> */ {
    var isWalkSeen = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Utility.setLocale(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        isWalkSeen = getPreferencesBoolean(this, Constants.IS_WALKTHROUGH_SEEN)

        Handler(Looper.myLooper()!!).postDelayed({
            acceptDynamicLink()
        }, 2000)
    }


    private fun acceptDynamicLink() {
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData: PendingDynamicLinkData? ->
                if (pendingDynamicLinkData != null) {
                    val deepLink: Uri? = pendingDynamicLinkData.link
                    val email = deepLink?.getQueryParameter("email") ?: ""
                    val chid = deepLink?.getQueryParameter("id") ?: ""
                    val path = deepLink?.path
                    if (path == "/reset-my-password/") {
                        // ResetPasswordActivity.open(this, email)
                    } else if (path == "/verify-my-account/") {
                        if (isLogin(this)) {
                            val bundle = Bundle()
                            bundle.putString(FROM, FROM_VERIFY_ACCOUNT)
                            bundle.putString(EMAIL, email)
                          //  AuthViewsHolderActivity.open(this, bundle)
                        } else {
                            val bundle = Bundle()
                            bundle.putString(FROM, FROM_VERIFY_ACCOUNT)
                            bundle.putString(EMAIL, email)
                          //  AuthViewsHolderActivity.open(this, bundle)
                        }
                    } else if (path == "/send-invitation-link/") {
                        val id = deepLink.getQueryParameter("id") ?: ""
                        val bundle = Bundle()
                        bundle.putString(FROM, FROM_INVITATION_LINK)
                        bundle.putString(USER_ID, id)
                       // AuthViewsHolderActivity.open(this, bundle)
                    } else if (path == "/share-biography-link/") {
                        if (isLogin(this)) {
                            val bundle = Bundle()
                            bundle.putString(FROM, DYNAMIC_LINK)
                            bundle.putString(Constants.USER_ID, chid)

                            AboutCharacterActivity.open(this, bundle)

                        } else {
                            val bundle = Bundle()
                            bundle.putString(FROM, FROM_BIO_LINK)
                            bundle.putString(Constants.CHARACTER_ID, chid)
                           // AuthViewsHolderActivity.open(this, bundle)
                        }
                    }
                    finishAffinity()
                } else {
                    if (isWalkSeen) loginFlow() else WalkThroughActivity.open(this)
                }
            }
            .addOnFailureListener(this) {
                if (isWalkSeen) loginFlow() else WalkThroughActivity.open(this)
            }
    }

    private fun loginFlow() {
        AboutCharacterActivity.isBackFromSplash=false
        NotificationsActivity.isNotification=false
        if (!isLogin(this)) {
            AuthViewsHolderActivity.open(this, null)
        } else {
            val extras = intent.extras
            if (extras != null) {
                extras.putString(FROM, NOTIFICATION)
                when (intent.extras?.getString("type")) {
                    "biosBirthday" -> {
                        val userString = intent.extras?.getString("user")
                        if (!userString.isNullOrBlank()) {
                            val userFromJSON = Gson().fromJson(userString, User::class.java)
                            val userId = userFromJSON._id
                            val name = userFromJSON.name
                            intent.putExtra(USER_ID, userId)
                            intent.putExtra(Constants.USER_NAME, name)
                            intent.removeExtra(FROM)
                            AboutCharacterActivity.isBackFromSplash=true
                            AboutCharacterActivity.open(this, intent.extras ?: bundleOf())
                            finishAffinity()
                            return
                        }
                    }
                    "uploadMonth" -> {

                        val userString = intent.extras?.getString("user")
                        if (!userString.isNullOrBlank()) {
                            val userFromJSON = Gson().fromJson(userString, User::class.java)
                            val userId = userFromJSON.postData?._id
                            val userName = userFromJSON.postData?.name

                            intent.putExtra(CHARACTER_ID, userId)
                             intent.removeExtra(FROM)
                             intent.putExtra(Constants.CHARACTER_DATA, CharacterData(null,null,userName,null,null,null,null,null,null,null,null,null,null,null))
                            AddNewPostActivity.open(this, intent.extras ?: bundleOf())
                            finishAffinity()
                            return
                        }
                        }
                        "accessingTime" -> {
                            DashboardActivity.open(this, bundleOf())
                            finishAffinity()
                            return
                        }
                        "afterRegistration" -> {
                            AddCharacterListingActivity.open(this)
                            finishAffinity()
                            return
                        }
                        else -> {

                            NotificationsActivity.isNotification=true
                            NotificationsActivity.open(this, extras)
                            finishAffinity()
                            return

                        }
                    }


                }
                /*        if (extras != null) {
                extras.putString(FROM, NOTIFICATION)
                for (key in extras.keySet()) {
                    val value = extras[key]
                    if (key == "type") {
                        NotificationsActivity.open(this, extras)
                        finishAffinity()
                        return
                    }
                }
            }*/
                /*val bundle=intent.extras
            println("notificationType: ${bundle?.getString(NOTIFICATION_TYPE)}")
            if (bundle?.getString(NOTIFICATION_TYPE)!=null) {
                NotificationsActivity.open(this)

            }else {*/
                val bundle = Bundle()
                bundle.putString(FROM, FROM_SPLASH)
                DashboardActivity.open(this, bundle)
                //}
            }
            finishAffinity()
        }
        //for getting the notification data when app is in background
/*    val extras = intent.extras
    if (extras != null) {
        for (key in extras.keySet()) {
            val value = extras[key]
            if (key == NOTIFICATION_TYPE) {
                NotificationsActivity.open(this)
                finishAffinity()
                return
            }}
    }

    AuthViewsHolderActivity.open(this, null)*/
    }
