package com.familypedia.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.familypedia.R
import com.familypedia.network.*
import com.familypedia.repository.FriendsRepository
import com.familypedia.utils.isInternetAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Response

class FriendsViewModel(application: Application) : BaseViewModel(application) {

    lateinit var friendsRepository: FriendsRepository
    val scope = CoroutineScope(Dispatchers.IO)
    var job: Job? = null
    var simpleResponseResult = MutableLiveData<SimpleResponseModel>()
    var userListResponseResult = MutableLiveData<UserListResponseModel>()
    var profileResponseResult = MutableLiveData<ProfileResponseModel>()
    var friendsProfileResponseResult = MutableLiveData<FriendProfileResponseModel>()
    var pendingRequestsResponseResult = MutableLiveData<PendingFriendRequestResponse>()
    val friendListResponseResult=MutableLiveData<FriendsListResponseModel>()

    fun init() {
        friendsRepository = FriendsRepository.instance
    }

    fun searchFriends(
        context: Context,
        request: SearchRequest
    ) {
        hitAPI(context, request, SEARCH_FRIENDS)
    }

    fun getFriendProfile(
        context: Context,
        userId: String
    ) {
        hitAPI(context, userId, FRIEND_PROFILE)
    }

    fun sendFriendRequest(
        context: Context,
        recipient_id: String
    ) {
        hitAPI(context, recipient_id, SEND_FRIEND_REQUEST)
    }

    fun unfriendUser(
        context: Context,
        unfriendUserRequest: UnfriendUserRequest
    ) {
        hitAPI(context, unfriendUserRequest, UNFRIEND_USER)
    }

    fun getPendingFriendRequests(
        context: Context,
        page:Int
    ) {
        hitAPI(context, page, PENDING_FRIEND_REQUEST)
    }

    fun getFriendsList(context: Context, request: SearchRequest) {
        hitAPI(context, request, GET_FRIEND_LIST)
    }

    fun acceptRejectFriendRequest(context: Context,request:AcceptRejectFriendRequest){
        hitAPI(context,request, ACCEPT_REJECT_FRIEND_REQUEST)
    }

    fun reportUser(
        context: Context,
        reportUserRequest: ReportUserRequest
    ) {
        hitAPI(context, reportUserRequest, REPORT_USER)
    }

    fun blockUser(
        context: Context,
        reportUserRequest: BlockUserRequest
    ) {
        hitAPI(context, reportUserRequest, BLOCK_USER)
    }

    fun unBlockUser(
        context: Context,
        unblockUserRequest: BlockUserRequest
    ) {
        hitAPI(context, unblockUserRequest, UNBLOCK_USER)
    }

    private fun hitAPI(context: Context, request: Any?, apiName: String) {
        if (context.isInternetAvailable()) {
            updateLoaderStatus.postValue(LoadingData(true, apiName))
            job = scope.launch {
                try {
                    var response: Response<Any>? = null
                    response = when (apiName) {
                        SEARCH_FRIENDS -> friendsRepository.searchFriends(request as SearchRequest) as Response<Any>
                        FRIEND_PROFILE -> friendsRepository.getFriendsProfile(request as String) as Response<Any>
                        SEND_FRIEND_REQUEST -> friendsRepository.sendFriendRequest(request as String) as Response<Any>
                        PENDING_FRIEND_REQUEST -> friendsRepository.getPendingFriendRequest(request as Int) as Response<Any>
                        GET_FRIEND_LIST -> friendsRepository.getFriendsList(request as SearchRequest) as Response<Any>
                        ACCEPT_REJECT_FRIEND_REQUEST -> friendsRepository.acceptRejectFriendRequest(
                            request as AcceptRejectFriendRequest
                        ) as Response<Any>
                        REPORT_USER -> friendsRepository.reportUser(request as ReportUserRequest) as Response<Any>
                        BLOCK_USER -> friendsRepository.blockUser(request as BlockUserRequest) as Response<Any>
                        UNBLOCK_USER -> friendsRepository.unBlockUser(request as BlockUserRequest) as Response<Any>
                        UNFRIEND_USER -> friendsRepository.unFriendUser(request as UnfriendUserRequest) as Response<Any>
                        else -> return@launch
                    }

                    when (response.isSuccessful) {
                        true -> {
                            when (response.code()) {
                                200, 201 -> {
                                    when (apiName) {
                                        SEARCH_FRIENDS -> {
                                            val responseBody =
                                                response.body() as UserListResponseModel
                                            responseBody.apiName = apiName
                                            userListResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(
                                                    false,
                                                    apiName
                                                )
                                            )
                                        }
                                        FRIEND_PROFILE -> {
                                            val responseBody =
                                                response.body() as FriendProfileResponseModel
                                            responseBody.apiName = apiName
                                            friendsProfileResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(
                                                    false,
                                                    apiName
                                                )
                                            )
                                        }
                                        SEND_FRIEND_REQUEST -> {
                                            val responseBody =
                                                response.body() as SimpleResponseModel
                                            responseBody.apiName = apiName
                                            simpleResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(
                                                    false,
                                                    apiName
                                                )
                                            )
                                        }
                                        PENDING_FRIEND_REQUEST -> {
                                            val responseBody =
                                                response.body() as PendingFriendRequestResponse
                                            responseBody.apiName = apiName
                                            pendingRequestsResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(false, apiName)
                                            )
                                        }
                                        GET_FRIEND_LIST -> {
                                            val responseBody =
                                                response.body() as FriendsListResponseModel
                                            responseBody.apiName = apiName
                                            friendListResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(false, apiName)
                                            )
                                        }
                                        ACCEPT_REJECT_FRIEND_REQUEST -> {
                                            val responseBody =
                                                response.body() as SimpleResponseModel
                                            responseBody.apiName = apiName
                                            simpleResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(false, apiName)
                                            )
                                        }

                                        REPORT_USER -> {
                                            val responseBody =
                                                response.body() as SimpleResponseModel
                                            responseBody.apiName = apiName
                                            simpleResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(false, apiName)
                                            )
                                        }

                                        BLOCK_USER -> {
                                            val responseBody =
                                                response.body() as SimpleResponseModel
                                            responseBody.apiName = apiName
                                            simpleResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(false, apiName)
                                            )
                                        }

                                        UNBLOCK_USER -> {
                                            val responseBody =
                                                response.body() as SimpleResponseModel
                                            responseBody.apiName = apiName
                                            simpleResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(false, apiName)
                                            )
                                        }
                                        UNFRIEND_USER -> {
                                            val responseBody =
                                                response.body() as SimpleResponseModel
                                            responseBody.apiName = apiName
                                            simpleResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(false, apiName)
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    updateLoaderStatus.postValue(LoadingData(false, apiName))
                                    parseError(
                                        response.errorBody(),
                                        response.code(),
                                        apiName
                                    )
                                }
                            }
                        }
                        false -> {
                            updateLoaderStatus.postValue(LoadingData(false, apiName))
                            parseError(
                                response.errorBody(), response.code(),
                                apiName
                            )
                        }
                    }
                } catch (e: Exception) {
                    catchException(e, apiName)
                    updateLoaderStatus.postValue(LoadingData(false, apiName))
                }
            }
        } else
            exceptions.postValue(ExceptionData(context.getString(R.string.no_internet), apiName))
    }

}