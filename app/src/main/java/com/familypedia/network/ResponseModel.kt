package com.familypedia.network

import com.familypedia.utils.FRIEND_STATUS_ENUM
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ExceptionData(
    val message: String,
    val apiName: String,
)

data class LoadingData(
    val status: Boolean,
    val apiName: String,
)

data class ErrorData(
    val message: String,
    val apiName: String,
    val status: Int,
)

class SimpleResponseModel(
    val status: Int?,
    var apiName: String?,
    val message: String?,
)

class NotificationCountResponseModel(
    val status: Int?,
    var apiName: String?,
    val message: String?,
    val data: NotificationCountData?
)

class NotificationCountData(
    val unread_notification_count: Int
)

class AuthResponseModel(
    val status: Int?,
    val message: String?,
    val data: Data?,
    var apiName: String?,
    var value: String?,


)
data class ProfileResponseModel(
    val status: Int?,
    val message: String?,
    val data: Data?,
    var apiName: String?,
)

data class FriendProfileResponseModel(
    val status: Int?,
    val message: String?,
    val data: FriendProfile?,
    var apiName: String?,
)

data class FriendProfile(
    val friend: User?,
    val status: String?,
    val character:CharacterOuterData
)


data class Data(
    val user: User?
)
data class User(
    var _id: String?,
      val name: String?,
    val country_code: String?,
    val auth_token: String?,
    val city: String?,
    val country: String?,
    val phone_number: String?,
    val date_of_birth: String?,
    val gender: String?,
    val time_stamp: String?,
    val email: String?,
    val profile_pic: String?,
    val notification_status: Boolean?,
    val is_account_active: Boolean?,
    val last_login: String?,
    val account_type: String?,
    val is_verified: Boolean?,
/*    val friends: List<String>?,
    val friendrequestsent: List<String>?*/
    var isFriend: Int,
    var isPending: Int,
    var isBlocked: Boolean,
    var isRecived: Int,
    var block_status: String,
    val is_first_login: Boolean?,
    val otp: String?,
    var isPhoneNumberVerify:Boolean,
    var email_verify:Boolean,
    val postData: PostData?=null,


    ) : Serializable


data class UserListResponseModel(
    val status: Int?,
    val message: String?,
    val data: UserOuterData?,
    var apiName: String?,
)

data class UserOuterData(
    @SerializedName("docs")
    val user: List<User>?,
    val limit: Int?,
    val totalPages: Int?,
    val totalDocs: Int?,
)

data class CharactersListResponseModel(
    val status: Int?,
    val message: String?,
    val data: CharacterOuterData?,
    var apiName: String?,
)

data class CharacterOuterData(
    @SerializedName("docs")
    val data: List<CharacterData>?,
    val limit: Int?,
    val totalPages: Int?,
    val totalDocs: Int?,
)

data class CharacterData(
    val profile_pic: String?,
    val _id: String?,
    val name: String?,
    val status: String?,
    val date_of_birth: String?,
    val date_of_death: String?,
    val city_of_birth: String?,
    val country_of_birth: String?,
    val permittedUser: List<String>?,
    val characterEditDeleterequestUser: List<String>?,
    val bookmark: List<Bookmark>?,
    val userId: String?,
    @SerializedName("createdBy")
    val createdBy: User?,
    val images: List<String?>?

) : Serializable

data class Bookmark(
    val _id: String?
) : Serializable

data class ImagesData(
    val attachPhotos: List<String>?
) : Serializable


data class CharactersResponseModel(
    val status: Int?,
    val message: String?,
    val data: CharacterData?,
    var apiName: String?,
)

data class TimelineListResponse(
    val status: Int?,
    val message: String?,
    val data: List<PostData>,
    var apiName: String?
)

data class PostListResponse(
    val status: Int?,
    val message: String?,
    val data: AllData,
    var apiName: String?
)

data class AllData(
    @SerializedName("docs")
    val data: List<PostData>?,
    val limit: Int?,
    val totalPages: Int?,
    val totalDocs: Int?,
)

data class PostData(
    val _id: String?,
    var attachPhotos: List<String>?,
    var startingDate: String?,
    var endingDate: String?,
    var eventType: String?,
    var description: String?,
    var location: String?,
    @SerializedName("postedBy")
    val postedBy: User?,
    @SerializedName("character")
    val character: CharacterData?,
    var name: String?=null,

    ) : Serializable

data class PermissionResponse(
    val status: Int?,
    val message: String?,
    val data: AllPermissionData?,
    var apiName: String?
)

data class AllPermissionData(
    @SerializedName("docs")
    val data: List<PermissionData>?,
    val limit: Int?,
    val totalPages: Int?,
    val totalDocs: Int?,
)

data class PermissionData(
    val _id: String?,
    val userId: String,
    val name: String?,
    val profile_pic: String?,
    val permittedUser: List<User>?,
    val characterEditDeleterequestUser: List<User>?
)

data class PendingFriendRequestResponse(
    val status: Int?,
    val message: String?,
    val data: RequestOuterData?,
    var apiName: String?
)

data class RequestOuterData(
    val requests: RequestData?,
)

data class RequestData(
    val docs: List<RequestList>,
    val limit: Int?,
    val totalPages: Int?,
    val totalDocs: Int?,
)

data class RequestList(
    //@SerializedName("_id")
    //val requestId:String?,
    val requester: User?,
    val recipient: User?,
)

//FRIEND LIST
data class FriendsListResponseModel(
    val status: Int?,
    val message: String?,
    val data: FriendOuterData?,
    var apiName: String?,
)

data class FriendOuterData(
    val docs: List<FriendInnerData>,
    val totalPages: Int?,
    val page: Int,
)

data class FriendInnerData(
    @SerializedName("friend_id")
    val friend: User?
)

data class NotificationResponseModel(
    val status: Int?,
    val message: String?,
    val data: NotificationDataOuter?,
    var apiName: String?,
)

data class NotificationDataOuter(
    val docs: List<NotificationData>,
    val totalPages: Int?,
    val page: Int?,
)

data class NotificationData(
    val _id:String?,
    val user_id: String?,
    val sender_user: User?,
    val title: String?,
    val type: String?,
    val body: String?,
    val time_stamp: Long?,
    val notification_type: String?,
    val character: CharacterData?,
    val postId: String?,
    var read_status: Boolean?

)

/*{
    "status": 200,
    "message": "Invite list fetched successfully",
    "data": {
    "docs": [
    {
        "profile_pic": null,
        "_id": "6215fa0d10c6b2063db7b5bc",
        "name": "ashraf husain",
        "created": "2022-02-23T09:10:37.994Z",
        "updated": "2022-02-23T09:10:37.994Z"
    }
    ],
    "totalDocs": 1,
    "limit": 10,
    "totalPages": 0,
    "page": 1
}
}*/

data class InvitationListResponse(
    val status: Int?,
    val message: String?,
    val data: InvitationOuterData?,
    var apiName: String?
)

data class InvitationOuterData(
    val docs: List<InvitationData>?,
    val totalPages: Int?,
    val page: Int?

)

data class InvitationData(
    val profile_pic: String?,
    val _id: String?,
    val name: String?,
    val created: String,

    )

class CreatePostResponseModel(
    val status: Int?,
    val message: String?,
    val data: PostData?,
    var apiName: String?
)

class FriendStatus(
    val type: FRIEND_STATUS_ENUM,
    val userId: String,
    val status: Boolean
)