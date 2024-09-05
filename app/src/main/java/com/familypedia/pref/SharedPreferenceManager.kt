package com.familypedia.pref

import android.content.SharedPreferences
import com.familypedia.network.User
import com.familypedia.utils.Utility
import java.lang.reflect.Type

class SharedPreferenceManager constructor(private val sharedPreferences: SharedPreferences) {

    companion object {
        const val SHARED_PREF_FILE_NAME = "familyPedia_shared_pre_file"
        const val ACCOUNTS = "ACCOUNTS"
    }

    fun addAccount(user: User) {
        if (getAccounts()=="") {
            val arrayList= arrayListOf<User>();
            arrayList.add(user)
            sharedPreferences.edit().putString(ACCOUNTS, arrayList.toString()).apply()
        }else{

        }

    }

    fun getAccounts(): String? {
        if (sharedPreferences.getString(ACCOUNTS, null)==null){
            return ""
        }else {
            return sharedPreferences.getString(ACCOUNTS, null)
        }
    }


    fun putObject(key: String, model: Any) {
        val stringData: String = Utility.jsonFromModel(model).toString()
        sharedPreferences.edit().putString(key, stringData).apply()
    }

    fun getObject(key: String, type: Type): Any? {
        val value = sharedPreferences.getString(key, null)
        return Utility.fromJson(value, type)
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}