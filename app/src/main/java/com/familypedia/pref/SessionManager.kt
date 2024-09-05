package com.familypedia.pref

import com.familypedia.network.User

object SessionManager {

    fun addNewAccount(user: User) {
        ///this.loginModel = loginModel
        SharedPreferenceFactory.getSharedPreferenceManager()
            .addAccount(user)
    }


    fun getAllAccounts(): String? {
        return SharedPreferenceFactory.getSharedPreferenceManager()
            .getAccounts()
    }


    fun clear() {
        SharedPreferenceFactory.getSharedPreferenceManager().clear()
    }

}