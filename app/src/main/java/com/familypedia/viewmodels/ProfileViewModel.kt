package com.familypedia.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.familypedia.R
import com.familypedia.network.*
import com.familypedia.repository.ProfileRepository
import com.familypedia.utils.isInternetAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class ProfileViewModel(application: Application):BaseViewModel(application) {
    lateinit var profileRepository: ProfileRepository
    val scope = CoroutineScope(Dispatchers.IO)
    var job: Job? = null
    var profileResponseResult = MutableLiveData<ProfileResponseModel?>()
    var verifyAuthTypeResult = MutableLiveData<ProfileResponseModel?>()
    fun init() {
        profileRepository = ProfileRepository.instance
    }


    fun updateProfile(
        file: MultipartBody.Part?,
        requestPart: MutableMap<String, RequestBody>,
        context: Context
    ) {
        if (context.isInternetAvailable()) {
            updateLoaderStatus.postValue(LoadingData(true,EDIT_PROFILE))
            job = scope.launch {
                try {
                    val response = profileRepository.updateProfile(requestPart,file)
                    when (response.isSuccessful) {
                        true -> {
                            when (response.code()) {
                                200, 201 -> {
                                    val responseBody = response.body()
                                    responseBody?.apiName = EDIT_PROFILE
                                    profileResponseResult.postValue(responseBody)
                                    updateLoaderStatus.postValue(LoadingData(false,EDIT_PROFILE))
                                }
                                else -> {
                                    updateLoaderStatus.postValue(LoadingData(false,EDIT_PROFILE))
                                    parseError(response.errorBody(), response.code(), EDIT_PROFILE)
                                }
                            }
                        }
                        false -> {
                            updateLoaderStatus.postValue(LoadingData(false,EDIT_PROFILE))
                            parseError(
                                response.errorBody(), response.code(),
                                EDIT_PROFILE
                            )
                        }
                    }
                } catch (e: Exception) {
                    catchException(e,EDIT_PROFILE)
                    updateLoaderStatus.postValue(LoadingData(false,EDIT_PROFILE))
                }
            }
        } else
            exceptions.postValue(ExceptionData(context.getString(R.string.no_internet),EDIT_PROFILE))
    }

    fun verifyAUthType(
        resendOtpRequest: ResendOtpRequest,
        context: Context
    ) {
        if (context.isInternetAvailable()) {
            updateLoaderStatus.postValue(LoadingData(true,EDIT_PROFILE))
            job = scope.launch {
                try {
                    val response = profileRepository.sendOTP(resendOtpRequest)
                    when (response.isSuccessful) {
                        true -> {
                            when (response.code()) {
                                200, 201 -> {
                                    val responseBody = response.body()
                                    responseBody?.apiName = EDIT_PROFILE
                                    verifyAuthTypeResult.postValue(responseBody)
                                    updateLoaderStatus.postValue(LoadingData(false,EDIT_PROFILE))
                                }
                                else -> {
                                    updateLoaderStatus.postValue(LoadingData(false,EDIT_PROFILE))
                                    parseError(response.errorBody(), response.code(), EDIT_PROFILE)
                                }
                            }
                        }
                        false -> {
                            updateLoaderStatus.postValue(LoadingData(false,EDIT_PROFILE))
                            parseError(
                                response.errorBody(), response.code(),
                                EDIT_PROFILE
                            )
                        }
                    }
                } catch (e: Exception) {
                    catchException(e,EDIT_PROFILE)
                    updateLoaderStatus.postValue(LoadingData(false,EDIT_PROFILE))
                }
            }
        } else
            exceptions.postValue(ExceptionData(context.getString(R.string.no_internet),EDIT_PROFILE))
    }

}