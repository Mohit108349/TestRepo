package com.familypedia.network

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

const val LOGIN = "api/v1/app/auth/login"
const val SIGNUP = "api/v1/app/auth/signup"
const val VERIFY_OTP = "/api/v1/app/auth/otp-verify"
const val VERIFY_ACCOUNT = "api/v1/app/auth/verify"
const val FORGOT_PASSWORD = "api/v1/app/auth/forgot-password"
const val RESEND_OTP = "api/v1/app/auth/resend"
const val SEND_VERIFY_OTP = "api/v1/app/auth/send-otp"
const val LOGOUT = "api/v1/app/auth/logout"
const val DEVICE_INFO = "api/v1/app/auth/device"
const val USER_ACTIVE = "api/v1/app/user/active"
const val VERIFY_PASSWORD = "api/v1/app/auth/verify-password"
const val DELETE_USER = "api/v1/app/auth/delete-user"
const val DELETE_BIO = "api/v1/app/character/{characterId}"
const val REPORT_USER = "/api/v1/app/report/report-user"
const val BLOCK_USER = "/api/v1/app/user/block-user"
const val UNBLOCK_USER = "/api/v1/app/user/unblock-user"
const val RESET_PASSWORD = "api/v1/app/auth/reset-password"
const val CHANGE_PASSWORD = "api/v1/app/auth/change-password"
const val EDIT_PROFILE = "api/v1/app/user/edit-profile"
const val ADD_NEW_CHARACTER = "/api/v1/app/character/add-new-character"
const val CHARACTER_BY_USER = "/api/v1/app/character/character-by-user"
const val GET_CHARACTER_PROFILE = "/api/v1/app/character/character-profile"
const val EDIT_CHARACTER = "/api/v1/app/character/edit-character"
const val CREATE_POST = "/api/v1/app/post/create-post"
const val HOME_POST_LIST = "api/v1/app/post/home-post-list"
const val CHARACTER_TIMELINE = "api/v1/app/post/character-timeline"
const val GET_MY_CHARACTERS_AND_PERMITTED_CHARACTERS_LIST =
    "/api/v1/app/character/user-character-with-permission-list"

const val UPDATE_POST = "/api/v1/app/post/edit-post"
const val DELETE_POST = "/api/v1/app/post/delete-post"
const val REPORT_POST = "/api/v1/app/report/report-post"
const val REQUEST_CHARACTER_PERMISSION = "/api/v1/app/character/character-request-permission"
const val CHARACTER_PERMISSION_LIST = "/api/v1/app/character/get-character-permission-list"
const val ACCEPT_CHARACTER_PERMISSION = "/api/v1/app/character/character-accept-permission"
const val DECLINE_CHARACTER_PERMISSION =
    "/api/v1/app/character/character-request-permission-decline"
const val SEARCH_CHARACTER = "/api/v1/app/character/character-search"
const val REMOVE_GIVEN_CHARACTER_PERMISSION = "/api/v1/app/character/character-permission-remove"
const val USER_CHARACTER_PERMISSIONS_LIST = "/api/v1/app/character/user-character-permission-list"
const val ADD_REMOVE_BOOKMARK = "/api/v1/app/character/character-bookmark"
const val BOOKMARK_LIST = "/api/v1/app/character/character-bookmark-list"
const val SEARCH_FRIENDS = "/api/v1/app/friend/friend-search"
const val FRIEND_PROFILE = "/api/v1/app/friend/friend-profile"
const val SEND_FRIEND_REQUEST = "/api/v1/app/friend/send-friend-request"
const val UNFRIEND_USER = "/api/v1/app/friend/unfriend"
const val PENDING_FRIEND_REQUEST = "/api/v1/app/friend/pending-friend-request"
const val GET_FRIEND_LIST = "/api/v1/app/friend/friend-list"
const val ACCEPT_REJECT_FRIEND_REQUEST = "/api/v1/app/friend/accept-friend-request"
const val GET_CHARACTER_BY_OTHER_USER = "/api/v1/app/character/character-by-other-user"
const val REMOVE_POST_PHOTOS = "/api/v1/app/post/remove-post-photos"
const val RESEND_SIGNUP = "/api/v1/app/auth/resendsignup"
const val GET_NOTIFICATION_HISTORY = "api/v1/app/notification/history"
const val GET_INVITATION_LIST = "api/v1/app/user/invite-list"
const val GET_NOTIFICATION_COUNT = "api/v1/app/notification/notification-count"
const val READ_NOTIFICATION = "api/v1/app/notification/notification-read"
const val POST_DETAIL = "api/v1/app/post/post-detail"


interface FamilyPediaServices {

    @POST(READ_NOTIFICATION)
    suspend fun readNotification(@Body request: ReadNotificationRequest): Response<SimpleResponseModel>

    @GET(GET_NOTIFICATION_COUNT)
    suspend fun getNotificationCount(): Response<NotificationCountResponseModel>
    @GET(USER_ACTIVE)
    suspend fun userActive(): Response<Any>

    @POST(LOGIN)
    suspend fun userLogin(@Body request: LoginRequest): Response<AuthResponseModel>

    @POST(RESEND_OTP)
    suspend fun resendOTP(@Body request: ResendOtpRequest): Response<AuthResponseModel>


    @POST(SIGNUP)
    suspend fun userSignup(@Body request: SignupRequest): Response<AuthResponseModel>

    @POST(SIGNUP)
    suspend fun userSignupNew(@Body request: SignupRequestNew): Response<AuthResponseModel>

//    @POST(VERIFY_OTP)
//    suspend fun userVerifyOtp(@Body request: VerifyOTPRequest): Response<AuthResponseModel>
    @GET(VERIFY_OTP)
    suspend fun userVerifyOtp(@Query("email") email :String?,
                              @Query("countryCode") countryCode :String?,
                              @Query("phoneNumber") phoneNumber :String?,
                              @Query("otp") otp :String?): Response<AuthResponseModel>

    @POST(FORGOT_PASSWORD)
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<AuthResponseModel>

    @POST(RESEND_OTP)
    suspend fun resendOTP(@Body request: ForgotPasswordRequest): Response<AuthResponseModel>
    @POST(SEND_VERIFY_OTP)
    suspend fun sendOTP(@Body request: ResendOtpRequest): Response<ProfileResponseModel>

    @POST(FORGOT_PASSWORD)
    suspend fun resetPassword(@Body request: ForgotPasswordRequest): Response<SimpleResponseModel>

    @POST(LOGOUT)
    suspend fun logout(): Response<SimpleResponseModel>

    @POST(DEVICE_INFO)
    suspend fun deviceInfo(@Body request: DeviceInfoRequest): Response<SimpleResponseModel>

    @POST(RESEND_SIGNUP)
    suspend fun resendSignupLink(@Body request: ResendSignupLinkRequest): Response<SimpleResponseModel>

    @JvmSuppressWildcards
    @Multipart
    @PUT(EDIT_PROFILE)
    suspend fun updateProfile(
        @Part profile_pic: MultipartBody.Part?,
        @PartMap(encoding = "UTF-8") params: Map<String, RequestBody>,
    ): Response<ProfileResponseModel>

    @PATCH(CHANGE_PASSWORD)
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<SimpleResponseModel>

    @JvmSuppressWildcards
    @Multipart
    @POST(ADD_NEW_CHARACTER)
    suspend fun addNewCharacter(
        @Part profile_pic: MultipartBody.Part?,
        @PartMap(encoding = "UTF-8") params: Map<String, RequestBody>,
    ): Response<CharactersResponseModel>

    @GET(CHARACTER_BY_USER)
    suspend fun getCharacterByUser(
        @Query("page") page: Int,
        @Query("search_text") searchText: String
    ): Response<CharactersListResponseModel>

    @GET(GET_MY_CHARACTERS_AND_PERMITTED_CHARACTERS_LIST)

    suspend fun getMyCharacterAndPermittedCharactersList(
        @Query("page") page: Int,
        @Query("search_text") searchText: String
    ): Response<CharactersListResponseModel>

    @GET(USER_CHARACTER_PERMISSIONS_LIST)
    suspend fun getCharacterWithPermissionUser(@Query("page") page: Int): Response<CharactersListResponseModel>

    @GET(GET_CHARACTER_PROFILE)
    suspend fun getCharacterProfile(@Query("characterId") characterId: String): Response<CharactersResponseModel>

    @JvmSuppressWildcards
    @Multipart
    @PUT(EDIT_CHARACTER)
    suspend fun editCharacter(
        @Part profile_pic: MultipartBody.Part?,
        @PartMap(encoding = "UTF-8") params: Map<String, RequestBody>,
    ): Response<CharactersResponseModel>

    @GET(VERIFY_ACCOUNT)
    suspend fun verifyAccount(@Query("email") email: String): Response<AuthResponseModel>

    @JvmSuppressWildcards
    @Multipart
    @POST(CREATE_POST)
    suspend fun createPost(
        @Part photos: List<MultipartBody.Part?>,
        @PartMap(encoding = "UTF-8") params: Map<String, RequestBody>,
    ): Response<CreatePostResponseModel>

    @JvmSuppressWildcards
    @Multipart
    @PUT(UPDATE_POST)
    suspend fun updatePost(
        @Part photos: List<MultipartBody.Part?>,
        @PartMap(encoding = "UTF-8") params: Map<String, RequestBody>,
    ): Response<CreatePostResponseModel>


    @GET(HOME_POST_LIST)
    suspend fun getHomePosts(
        @Query("search_text") searchText: String,
        @Query("page") page: Int
    ): Response<PostListResponse>

    @GET(CHARACTER_TIMELINE)
    suspend fun getCharacterPosts(
        @Query("characterId") characterId: String
        /*@Query("search_text") searchText: String,
        @Query("page") page: Int*/
    ): Response<TimelineListResponse>


    @DELETE(DELETE_POST)
    suspend fun deletePost(@Query("postId") postId: String): Response<SimpleResponseModel>

    @POST(REPORT_POST)
    suspend fun reportPost(@Body reportPostRequest: ReportPostRequest): Response<SimpleResponseModel>

    @PATCH(REQUEST_CHARACTER_PERMISSION)
    suspend fun requestCharacterPermission(@Body request: AskCharacterPermissionRequest): Response<SimpleResponseModel>

    @GET(CHARACTER_PERMISSION_LIST)
    suspend fun getCharactersPermissionList(@Query("page") page: Int): Response<PermissionResponse>

    @PATCH(ACCEPT_CHARACTER_PERMISSION)
    suspend fun acceptCharacterPermission(@Body request: AskCharacterPermissionRequest): Response<SimpleResponseModel>

    @PATCH(DECLINE_CHARACTER_PERMISSION)
    suspend fun declineCharacterPermission(@Body request: AskCharacterPermissionRequest): Response<SimpleResponseModel>

    @GET(SEARCH_CHARACTER)
    suspend fun searchCharacter(
        @Query("search_text") searchText: String,
        /* @Query("search") email: String,*/
        @Query("page") page: Int
    ): Response<CharactersListResponseModel>

    @PATCH(REMOVE_GIVEN_CHARACTER_PERMISSION)
    suspend fun removeGivenCharacterPermission(@Body request: AskCharacterPermissionRequest): Response<SimpleResponseModel>

    @PATCH(ADD_REMOVE_BOOKMARK)
    suspend fun addRemoveBookmark(@Body request: BookmarkRequest): Response<SimpleResponseModel>

    @GET(BOOKMARK_LIST)
    suspend fun getBookmarkList(
        @Query("search_text") searchText: String,
        @Query("page") page: Int
    ): Response<CharactersListResponseModel>

    @GET(SEARCH_FRIENDS)
    suspend fun getSearchFriendsList(
        @Query("search_text") searchText: String,
        @Query("page") page: Int
    ): Response<UserListResponseModel>

    @GET(FRIEND_PROFILE)
    suspend fun getFriendProfile(@Query("friendId") userId: String): Response<FriendProfileResponseModel>


    @POST(SEND_FRIEND_REQUEST)
    suspend fun sendFriendRequest(@Body recipient_id: JsonObject): Response<SimpleResponseModel>

    @GET(PENDING_FRIEND_REQUEST)
    suspend fun getPendingFriendRequest(
        @Query("page") page: Int
    ): Response<PendingFriendRequestResponse>

    @GET(GET_FRIEND_LIST)
    suspend fun getFriendsList(
        @Query("search_text") searchText: String,
        @Query("page") page: Int
    ): Response<FriendsListResponseModel>

    @POST(ACCEPT_REJECT_FRIEND_REQUEST)
    suspend fun acceptRejectFriendRequest(@Body request: AcceptRejectFriendRequest): Response<SimpleResponseModel>

    @GET(GET_CHARACTER_BY_OTHER_USER)
    suspend fun getCharacterByAnotherUser(
        @Query("userId") userId: String,
        @Query("page") page: Int
    ): Response<CharactersListResponseModel>

    @PATCH(REMOVE_POST_PHOTOS)
    suspend fun removePostPhotos(
        @Query("postId") postId: String,
        @Query("index") index: Int
    ): Response<SimpleResponseModel>

    @GET(GET_NOTIFICATION_HISTORY)
    suspend fun getNotificationHistory(
        @Query("page") page: Int
    ): Response<NotificationResponseModel>

    @GET(GET_INVITATION_LIST)
    suspend fun getInvitationList(
        @Query("page") page: Int
    ): Response<InvitationListResponse>


    @GET(POST_DETAIL)
    suspend fun getPostDetail(
        @Query("postId") postId: String
    ): Response<CreatePostResponseModel>

    @DELETE(DELETE_USER)
    suspend fun deleteUserAccount(@Query("_id") id: String): Response<SimpleResponseModel>

    @DELETE(DELETE_BIO)
    suspend fun deleteUserBio(@Path("characterId") id: String): Response<SimpleResponseModel>

    @POST(REPORT_USER)
    suspend fun reportUser(@Body reportUserRequest: ReportUserRequest): Response<SimpleResponseModel>

    @POST(BLOCK_USER)
    suspend fun blockUser(@Body blockUser: BlockUserRequest): Response<SimpleResponseModel>

    @POST(UNBLOCK_USER)
    suspend fun unBlockUser(@Body unblockUser: BlockUserRequest): Response<SimpleResponseModel>

    @PATCH(UNFRIEND_USER)
    suspend fun unfriendUser(@Body unfriendUserRequest: UnfriendUserRequest): Response<SimpleResponseModel>

    @POST(VERIFY_PASSWORD)
    suspend fun verifyPassword(@Body req: VerifyPassword): Response<SimpleResponseModel>

}