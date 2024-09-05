package com.familypedia.network

data class LoginRequest(val email: String?, val password: String?, val countryCode:String?,val phoneNumber:String?)
data class ResendOtpRequest(val email: String?, val countryCode:String?,val phoneNumber:String?)

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val dynamicLink: String,
    var invitation_id: String?
)

data class SignupRequestNew(
    val name: String,
    val password: String,
    val type: String,
    val countryCode: String?,
    var phoneNumber: String?,
    val email: String?,
)
data class VerifyOTPRequest(
    val phoneNumber: String?,
    val email: String?,
    val countryCode: String?,
    val otp:String?

)

data class EmailRequest(val email: String)
data class VerifyPassword(val password: String)
data class ForgotPasswordRequest(val email: String?,
                                 val phoneNumber: String?,
                                 val countryCode: String?,
                                 val otp: String?,
                                 val password: String?)
data class ChangePasswordRequest(val old_password: String, val new_password: String)

data class DeviceInfoRequest(
    val device_uid: String,
    val device_type: String,
    val device_token: String,
    val jwt_token: String
)

data class CreatePostRequest(
    val postId: String?,
    val characterId: String,
    val startingDate: String,
    val startingDateTimeStamp: String,
    val endingDate: String,
    val eventType: String,
    val description: String,
    val location: String,
    val imageList: List<String>?
)

data class AskCharacterPermissionRequest(
    val userId: String,
    val characterId: String,
)

data class SearchRequest(
    val searchText: String,
    val page: Int,
)

data class BookmarkRequest(
    val characterId: String,
    val bookmark: Boolean
)

data class AcceptRejectFriendRequest(
    val id: String,
    val status: Boolean?
)

data class RemovePostImageRequest(
    val postId: String,
    val index: Int,
)

data class ResendSignupLinkRequest(
    val dynamicLink: String,
    val email: String,
)

data class ReportPostRequest(
    val postId: String
)

data class ReportUserRequest(
    val userId: String
)

data class BlockUserRequest(
    val userId: String
)

data class UnfriendUserRequest(
    val userId: String
)

data class UserRequest(
    val userId: String,
    val page: Int
)
/*
{
    "_id": [
    "623809ed0b18653d5413eb2e",
    "6238193fc05f2b3f320c139d",
    "623819e5c05f2b3f320c13ae"]
}
*/


data class ReadNotificationRequest(
    val _id: List<String>?
)