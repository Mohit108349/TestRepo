package com.familypedia.pref

object SharedPreferenceFactory {
    private lateinit var sharedPreferenceManager: SharedPreferenceManager

    fun setSharedPreferenceManager(sharedPreferenceManager: SharedPreferenceManager) {
        this.sharedPreferenceManager = sharedPreferenceManager
    }

    fun getSharedPreferenceManager() = sharedPreferenceManager
}