package com.familypedia.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.familypedia.R
import com.familypedia.network.*
import com.familypedia.repository.NotificationRepository
import com.familypedia.repository.ProfileRepository
import com.familypedia.utils.isInternetAvailable
import com.familypedia.view.dashboard.home.HomeFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class NotificationViewModel(application: Application):BaseViewModel(application) {
    lateinit var notificationRepository: NotificationRepository
    val scope = CoroutineScope(Dispatchers.IO)
    var job: Job? = null
    var notificationResponseResult = MutableLiveData<NotificationResponseModel>()
    var invitationResponseResult = MutableLiveData<InvitationListResponse>()
    var unreadNotificationContResponseResult = MutableLiveData<NotificationCountResponseModel>()
    var simpleResponseResult = MutableLiveData<SimpleResponseModel>()


    fun init() {
        notificationRepository = NotificationRepository.instance
    }


    fun getNotifications(
        context: Context,
        page:Int
    ) {
        if (context.isInternetAvailable()) {
            updateLoaderStatus.postValue(LoadingData(true, GET_NOTIFICATION_HISTORY))
            job = scope.launch {
                try {
                    val response = notificationRepository.getNotifications(page)
                    when (response.isSuccessful) {
                        true -> {
                            when (response.code()) {
                                200, 201 -> {
                                    val responseBody = response.body()
                                    responseBody?.apiName = GET_NOTIFICATION_HISTORY
                                    notificationResponseResult.postValue(responseBody)
                                    updateLoaderStatus.postValue(LoadingData(false, GET_NOTIFICATION_HISTORY))
                                }
                                else -> {
                                    updateLoaderStatus.postValue(LoadingData(false, GET_NOTIFICATION_HISTORY))
                                    parseError(response.errorBody(), response.code(), GET_NOTIFICATION_HISTORY)
                                }
                            }
                        }
                        false -> {
                            updateLoaderStatus.postValue(LoadingData(false, GET_NOTIFICATION_HISTORY))
                            parseError(
                                response.errorBody(), response.code(),
                                GET_NOTIFICATION_HISTORY
                            )
                        }
                    }
                } catch (e: Exception) {
                    catchException(e, GET_NOTIFICATION_HISTORY)
                    updateLoaderStatus.postValue(LoadingData(false, GET_NOTIFICATION_HISTORY))
                }
            }
        } else
            exceptions.postValue(
                ExceptionData(context.getString(R.string.no_internet),
                    GET_NOTIFICATION_HISTORY
                )
            )
    }
    fun getUnreadNotifications(
        context: Context
    ) {
        if (context.isInternetAvailable()) {
            updateLoaderStatus.postValue(LoadingData(true, GET_NOTIFICATION_COUNT))
            job = scope.launch {
                try {
                    val response = notificationRepository.getUnreadNotificationsCount()
                    when (response.isSuccessful) {
                        true -> {
                            when (response.code()) {
                                200, 201 -> {
                                    val responseBody = response.body()
                                    responseBody?.apiName = GET_NOTIFICATION_COUNT
                                    unreadNotificationContResponseResult.postValue(responseBody)
                                    updateLoaderStatus.postValue(LoadingData(false, GET_NOTIFICATION_COUNT))
                                }
                                else -> {
                                    updateLoaderStatus.postValue(LoadingData(false, GET_NOTIFICATION_COUNT))
                                    parseError(response.errorBody(), response.code(), GET_NOTIFICATION_COUNT)
                                }
                            }
                        }
                        false -> {
                            updateLoaderStatus.postValue(LoadingData(false, GET_NOTIFICATION_COUNT))
                            parseError(
                                response.errorBody(), response.code(),
                                GET_NOTIFICATION_COUNT
                            )
                        }
                    }
                } catch (e: Exception) {
                    catchException(e, GET_NOTIFICATION_COUNT)
                    updateLoaderStatus.postValue(LoadingData(false, GET_NOTIFICATION_COUNT))
                }
            }
        } else
            exceptions.postValue(
                ExceptionData(context.getString(R.string.no_internet),
                    GET_NOTIFICATION_COUNT
                )
            )
    }
    fun readNotification(
        context: Context,
        request:ReadNotificationRequest
    ) {
        if (context.isInternetAvailable()) {
            updateLoaderStatus.postValue(LoadingData(true, READ_NOTIFICATION))
            job = scope.launch {
                try {
                    val response = notificationRepository.readNotifications(request)
                    when (response.isSuccessful) {
                        true -> {
                            when (response.code()) {
                                200, 201 -> {
                                    val responseBody = response.body()
                                    responseBody?.apiName = READ_NOTIFICATION
                                    simpleResponseResult.postValue(responseBody)
                                    updateLoaderStatus.postValue(LoadingData(false, READ_NOTIFICATION))
                                    HomeFragment.notificationLiveData.postValue(true)
                                }
                                else -> {
                                    updateLoaderStatus.postValue(LoadingData(false, READ_NOTIFICATION))
                                    parseError(response.errorBody(), response.code(), READ_NOTIFICATION)
                                }
                            }
                        }
                        false -> {
                            updateLoaderStatus.postValue(LoadingData(false, READ_NOTIFICATION))
                            parseError(
                                response.errorBody(), response.code(),
                                READ_NOTIFICATION
                            )
                        }
                    }
                } catch (e: Exception) {
                    catchException(e, READ_NOTIFICATION)
                    updateLoaderStatus.postValue(LoadingData(false, READ_NOTIFICATION))
                }
            }
        } else
            exceptions.postValue(
                ExceptionData(context.getString(R.string.no_internet),
                    READ_NOTIFICATION
                )
            )
    }

    fun getInvitationList(
        context: Context,
        page:Int
    ) {
        if (context.isInternetAvailable()) {
            updateLoaderStatus.postValue(LoadingData(true, GET_INVITATION_LIST))
            job = scope.launch {
                try {
                    val response = notificationRepository.getInvitationList(page)
                    when (response.isSuccessful) {
                        true -> {
                            when (response.code()) {
                                200, 201 -> {
                                    val responseBody = response.body()
                                    responseBody?.apiName = GET_INVITATION_LIST
                                    invitationResponseResult.postValue(responseBody)
                                    updateLoaderStatus.postValue(LoadingData(false, GET_INVITATION_LIST))
                                }
                                else -> {
                                    updateLoaderStatus.postValue(LoadingData(false, GET_INVITATION_LIST))
                                    parseError(response.errorBody(), response.code(), GET_INVITATION_LIST)
                                }
                            }
                        }
                        false -> {
                            updateLoaderStatus.postValue(LoadingData(false, GET_INVITATION_LIST))
                            parseError(
                                response.errorBody(), response.code(),
                                GET_INVITATION_LIST
                            )
                        }
                    }
                } catch (e: Exception) {
                    catchException(e, GET_INVITATION_LIST)
                    updateLoaderStatus.postValue(LoadingData(false, GET_INVITATION_LIST))
                }
            }
        } else
            exceptions.postValue(
                ExceptionData(context.getString(R.string.no_internet),
                    GET_INVITATION_LIST
                )
            )
    }

}