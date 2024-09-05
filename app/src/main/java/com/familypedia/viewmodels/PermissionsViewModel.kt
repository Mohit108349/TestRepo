package com.familypedia.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.familypedia.R
import com.familypedia.network.*
import com.familypedia.repository.AuthRepository
import com.familypedia.repository.PermissionRepository
import com.familypedia.utils.isInternetAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Response

class PermissionsViewModel(application: Application) : BaseViewModel(application) {
    lateinit var permissionRepository: PermissionRepository
    val scope = CoroutineScope(Dispatchers.IO)
    var job: Job? = null

    var simpleResponseResult = MutableLiveData<SimpleResponseModel>()
    var permisssionResponseResult = MutableLiveData<PermissionResponse>()
    fun init() {
        permissionRepository = PermissionRepository.instance
    }

    fun askCharacterPermission(
        context: Context,
        request: AskCharacterPermissionRequest
    ) {
        hitAPI(context, request, REQUEST_CHARACTER_PERMISSION)
    }

    fun getPermissionsList(
        context: Context,
        page: Int
    ) {
        hitAPI(context, page, CHARACTER_PERMISSION_LIST)
    }

    fun acceptDeclinePermission(
        context: Context,
        acceptRequest: Boolean,
        request: AskCharacterPermissionRequest
    ) {
        if (acceptRequest)
            hitAPI(context, request, ACCEPT_CHARACTER_PERMISSION)
        else
            hitAPI(context, request, DECLINE_CHARACTER_PERMISSION)
    }
    fun removeGivenCharacterPermission(
        context: Context,
        request: AskCharacterPermissionRequest
    ){
        hitAPI(context,request, REMOVE_GIVEN_CHARACTER_PERMISSION)
    }

    private fun hitAPI(context: Context, request: Any?, apiName: String) {
        if (context.isInternetAvailable()) {
            updateLoaderStatus.postValue(LoadingData(true,apiName))
            job = scope.launch {
                try {
                    var response: Response<Any>? = null
                    response = when (apiName) {
                        REQUEST_CHARACTER_PERMISSION -> permissionRepository.askPermission(request as AskCharacterPermissionRequest) as Response<Any>
                        CHARACTER_PERMISSION_LIST -> permissionRepository.getPermissionsList(request as Int) as Response<Any>
                        ACCEPT_CHARACTER_PERMISSION -> permissionRepository.acceptDeclineCharacterPermission(
                            true,
                            request as AskCharacterPermissionRequest
                        ) as Response<Any>
                        DECLINE_CHARACTER_PERMISSION -> permissionRepository.acceptDeclineCharacterPermission(
                            false,
                            request as AskCharacterPermissionRequest
                        ) as Response<Any>
                        REMOVE_GIVEN_CHARACTER_PERMISSION -> permissionRepository.removeGivenCharacterPermission(request as AskCharacterPermissionRequest) as Response<Any>


                        else -> return@launch
                    }

                    when (response.isSuccessful) {
                        true -> {
                            when (response.code()) {
                                200, 201 -> {
                                    when (apiName) {
                                        REQUEST_CHARACTER_PERMISSION -> {
                                            val responseBody =
                                                response.body() as SimpleResponseModel
                                            responseBody.apiName = apiName
                                            simpleResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(LoadingData(false,apiName))
                                        }
                                        CHARACTER_PERMISSION_LIST -> {
                                            val responseBody = response.body() as PermissionResponse
                                            responseBody.apiName = apiName
                                            permisssionResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(LoadingData(false,apiName))
                                        }
                                        ACCEPT_CHARACTER_PERMISSION -> {
                                            val responseBody =
                                                response.body() as SimpleResponseModel
                                            responseBody.apiName = apiName
                                            simpleResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(LoadingData(false,apiName))
                                        }
                                        DECLINE_CHARACTER_PERMISSION -> {
                                            val responseBody =
                                                response.body() as SimpleResponseModel
                                            responseBody.apiName = apiName
                                            simpleResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(LoadingData(false,apiName))
                                        }
                                        REMOVE_GIVEN_CHARACTER_PERMISSION -> {
                                            val responseBody =
                                                response.body() as SimpleResponseModel
                                            responseBody.apiName = apiName
                                            simpleResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(LoadingData(false,apiName))
                                        }
                                    }
                                }
                                else -> {
                                    updateLoaderStatus.postValue(LoadingData(false,apiName))
                                    parseError(
                                        response.errorBody(),
                                        response.code(),
                                        apiName
                                    )
                                }
                            }
                        }
                        false -> {
                            updateLoaderStatus.postValue(LoadingData(false,apiName))
                            parseError(
                                response.errorBody(), response.code(),
                                apiName
                            )
                        }
                    }
                } catch (e: Exception) {
                    catchException(e, apiName)
                    updateLoaderStatus.postValue(LoadingData(false,apiName))
                }
            }
        } else
            exceptions.postValue(ExceptionData(context.getString(R.string.no_internet), apiName))
    }


}