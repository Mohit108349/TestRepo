package com.familypedia

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.familypedia.utils.Constants
import com.familypedia.utils.Utility
import com.familypedia.utils.Utility.getCurrentFCMToken

class FamilyPediaApplication:Application() ,LifecycleObserver{

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        instance = this
        getCurrentFCMToken()
        Utility.setLocale(instance)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        Utility.setLocale(instance)
    }

    companion object {
        lateinit var instance: FamilyPediaApplication

        @JvmStatic
        fun getToken(): String {
            return Utility.getPreferencesString(instance, Constants.AUTH_TOKEN)
        }

        @JvmStatic
        fun getUserId(): String {
            return Utility.getPreferencesString(instance, Constants.USER_ID)
        }
    }
}