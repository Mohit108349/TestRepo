package com.familypedia.repository

import com.familypedia.network.*
import com.google.gson.JsonObject
import retrofit2.Response

object FriendsRepository {
    private var repository: FriendsRepository? = null
    private lateinit var api: FamilyPediaServices
    val instance: FriendsRepository
        get() {
            repository = FriendsRepository
            api = ServiceGenerator.createService(FamilyPediaServices::class.java)
            return repository!!
        }

    suspend fun searchFriends(
        searchRequest: SearchRequest
    ): Response<UserListResponseModel> {
        return api.getSearchFriendsList(searchRequest.searchText, searchRequest.page)
    }

    suspend fun getFriendsProfile(userId: String): Response<FriendProfileResponseModel> {
        return api.getFriendProfile(userId)
    }

    suspend fun sendFriendRequest(recepientId: String): Response<SimpleResponseModel> {
        val request = JsonObject()
        request.addProperty("recipient_id", recepientId)
        return api.sendFriendRequest(request)
    }

    suspend fun getPendingFriendRequest(page:Int): Response<PendingFriendRequestResponse> {
        return api.getPendingFriendRequest(page)
    }

    suspend fun getFriendsList(request: SearchRequest):Response<FriendsListResponseModel>{
        return api.getFriendsList(request.searchText,request.page)
    }

    suspend fun acceptRejectFriendRequest(acceptRejectFriendRequest: AcceptRejectFriendRequest):Response<SimpleResponseModel>{
        return api.acceptRejectFriendRequest(acceptRejectFriendRequest)
    }

    suspend fun reportUser(reportUserRequest: ReportUserRequest): Response<SimpleResponseModel> {
        return api.reportUser(reportUserRequest)
    }
    suspend fun blockUser(blockUserRequest: BlockUserRequest): Response<SimpleResponseModel> {
        return api.blockUser(blockUserRequest)
    }

    suspend fun unBlockUser(unblockUserRequest: BlockUserRequest): Response<SimpleResponseModel> {
        return api.unBlockUser(unblockUserRequest)
    }

    suspend fun unFriendUser(unfriendUserRequest: UnfriendUserRequest): Response<SimpleResponseModel> {
        return api.unfriendUser(unfriendUserRequest)
    }
}