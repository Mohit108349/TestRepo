package com.familypedia.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.familypedia.R
import com.familypedia.network.*
import com.familypedia.repository.CharacterRepository
import com.familypedia.utils.isInternetAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class CharacterViewModel(application: Application) : BaseViewModel(application) {
    lateinit var characterRepository: CharacterRepository
    val scope = CoroutineScope(Dispatchers.IO)
    var job: Job? = null
    var simpleResponseResult = MutableLiveData<SimpleResponseModel>()
    var createUpdatePostResult = MutableLiveData<CreatePostResponseModel>()
    var characterListResponseResult = MutableLiveData<CharactersListResponseModel>()
    var characterResponseResult = MutableLiveData<CharactersResponseModel>()
    var postsListResult = MutableLiveData<PostListResponse>()
    var timelineListResult = MutableLiveData<TimelineListResponse>()

    fun init() {
        characterRepository = CharacterRepository.instance
    }


    fun getUsersCharacterAndPermittedCharactersList(
        context: Context,
        page: SearchRequest
    ) {
        hitAPI(context, page, GET_MY_CHARACTERS_AND_PERMITTED_CHARACTERS_LIST)
    }

    fun getUsersCharacterList(
        context: Context,
        page: SearchRequest
    ) {
        hitAPI(context, page, CHARACTER_BY_USER)
    }

    fun getCharacterWithUserPermission(
        context: Context,
        page: Int
    ) {
        hitAPI(context, page, USER_CHARACTER_PERMISSIONS_LIST)
    }

    fun getCharacterProfile(
        context: Context,
        characterId: String
    ) {
        hitAPI(context, characterId, GET_CHARACTER_PROFILE)
    }

    fun createUpdatePost(
        request: CreatePostRequest,
        context: Context
    ) {
        hitAPI(context, request, CREATE_POST)
    }

    fun getHomePosts(
        context: Context,
        request: SearchRequest?
    ) {
        hitAPI(context, request, HOME_POST_LIST)
    }

    fun getTimeLinePosts(
        context: Context,
        characterId: String?
    ) {
        hitAPI(context, characterId, CHARACTER_TIMELINE)
    }

    fun deletePost(
        postId: String,
        context: Context
    ) {
        hitAPI(context, postId, DELETE_POST)
    }
    fun deleteBio(
        characterId: String,
        context: Context
    ) {
        hitAPI(context, characterId, DELETE_BIO)
    }

    fun reportPost(
        postId: ReportPostRequest,
        context: Context
    ) {
        hitAPI(context, postId, REPORT_POST)
    }

    fun searchCharacter(
        context: Context,
        request: SearchRequest
    ) {
        hitAPI(context, request, SEARCH_CHARACTER)
    }

    fun addRemoveBookmark(context: Context, request: BookmarkRequest) {
        hitAPI(context, request, ADD_REMOVE_BOOKMARK)
    }

    fun getBookmarkList(context: Context, request: SearchRequest) {
        hitAPI(context, request, BOOKMARK_LIST)
    }

    fun getCharactersByAnotherUser(context: Context, request: UserRequest) {
        hitAPI(context, request, GET_CHARACTER_BY_OTHER_USER)
    }

    fun removePostPhotos(context: Context, request: RemovePostImageRequest) {
        hitAPI(context, request, REMOVE_POST_PHOTOS)
    }

    fun getPostDetail(context: Context, postId: String) {
        hitAPI(context, postId, POST_DETAIL)
    }


    private fun hitAPI(context: Context, request: Any?, apiName: String) {
        if (context.isInternetAvailable()) {
            if (apiName != DELETE_POST)
                updateLoaderStatus.postValue(LoadingData(true, apiName))
            job = scope.launch {
                try {
                    var response: Response<Any>? = null
                    response = when (apiName) {
                        CHARACTER_BY_USER -> characterRepository.getCharactersByUser(request as SearchRequest) as Response<Any>
                        GET_MY_CHARACTERS_AND_PERMITTED_CHARACTERS_LIST -> characterRepository.getMyCharacterAndPermittedCharactersList(
                            request as SearchRequest
                        ) as Response<Any>
                        USER_CHARACTER_PERMISSIONS_LIST -> characterRepository.getCharactersWithPermissionsUser(
                            request as Int
                        ) as Response<Any>
                        GET_CHARACTER_PROFILE -> characterRepository.getCharactersProfile(request.toString()) as Response<Any>
                        /*EDIT_CHARACTER->characterRepository.editCharacter(request as EditCharacterRequest) as Response<Any>*/
                        CREATE_POST -> characterRepository.createUpdatePost(request as CreatePostRequest) as Response<Any>
                        HOME_POST_LIST -> characterRepository.getHomePosts(request as SearchRequest) as Response<Any>
                        CHARACTER_TIMELINE -> characterRepository.getTimeLinePosts(request as String) as Response<Any>

                        DELETE_BIO -> characterRepository.deleteUserBio(request as String) as Response<Any>
                        DELETE_POST -> characterRepository.deletePost(request as String) as Response<Any>
                        REPORT_POST -> characterRepository.reportPost(request as ReportPostRequest) as Response<Any>
                        SEARCH_CHARACTER -> characterRepository.searchCharacter(request as SearchRequest) as Response<Any>
                        ADD_REMOVE_BOOKMARK -> characterRepository.addRemoveBookmark(request as BookmarkRequest) as Response<Any>
                        BOOKMARK_LIST -> characterRepository.getBookmarkList(request as SearchRequest) as Response<Any>
                        GET_CHARACTER_BY_OTHER_USER -> characterRepository.getCharacterByAnotherUser(
                            request as UserRequest
                        ) as Response<Any>
                        REMOVE_POST_PHOTOS -> characterRepository.removePostImage(request as RemovePostImageRequest) as Response<Any>

                        POST_DETAIL -> characterRepository.getPostDetails(request as String) as Response<Any>


                        else -> return@launch
                    }

                    when (response.isSuccessful) {
                        true -> {
                            when (response.code()) {
                                200, 201 -> {
                                    when (apiName) {
                                        CHARACTER_BY_USER, GET_MY_CHARACTERS_AND_PERMITTED_CHARACTERS_LIST -> {
                                            val responseBody =
                                                response.body() as CharactersListResponseModel
                                            responseBody.apiName = apiName
                                            characterListResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(
                                                    false,
                                                    apiName
                                                )
                                            )
                                        }
                                        USER_CHARACTER_PERMISSIONS_LIST -> {
                                            val responseBody =
                                                response.body() as CharactersListResponseModel
                                            responseBody.apiName = apiName
                                            characterListResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(
                                                    false,
                                                    apiName
                                                )
                                            )
                                        }
                                        BOOKMARK_LIST -> {
                                            val responseBody =
                                                response.body() as CharactersListResponseModel
                                            responseBody.apiName = apiName
                                            characterListResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(
                                                    false,
                                                    apiName
                                                )
                                            )
                                        }
                                        GET_CHARACTER_PROFILE -> {
                                            val responseBody =
                                                response.body() as CharactersResponseModel
                                            responseBody.apiName = apiName
                                            characterResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(
                                                    false,
                                                    apiName
                                                )
                                            )
                                        }
                                        CREATE_POST -> {
                                            val responseBody =
                                                response.body() as CreatePostResponseModel
                                            responseBody.apiName = apiName
                                            createUpdatePostResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(
                                                    false,
                                                    apiName
                                                )
                                            )
                                        }
                                        HOME_POST_LIST -> {
                                            val responseBody =
                                                response.body() as PostListResponse
                                            responseBody.apiName = apiName
                                            postsListResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(
                                                    false,
                                                    apiName
                                                )
                                            )
                                        }
                                        CHARACTER_TIMELINE -> {
                                            val responseBody =
                                                response.body() as TimelineListResponse
                                            responseBody.apiName = apiName
                                            timelineListResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(
                                                    false,
                                                    apiName
                                                )
                                            )
                                        }

                                        DELETE_POST -> {
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
                                        DELETE_BIO -> {
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

                                        REPORT_POST -> {
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
                                        ADD_REMOVE_BOOKMARK -> {
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
                                        SEARCH_CHARACTER -> {
                                            val responseBody =
                                                response.body() as CharactersListResponseModel
                                            responseBody.apiName = apiName
                                            characterListResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(
                                                    false,
                                                    apiName
                                                )
                                            )
                                        }
                                        GET_CHARACTER_BY_OTHER_USER -> {
                                            val responseBody =
                                                response.body() as CharactersListResponseModel
                                            responseBody.apiName = apiName
                                            characterListResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(
                                                    false,
                                                    apiName
                                                )
                                            )

                                        }
                                        REMOVE_POST_PHOTOS -> {
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
                                        POST_DETAIL -> {
                                            val responseBody =
                                                response.body() as CreatePostResponseModel
                                            responseBody.apiName = apiName
                                            createUpdatePostResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(
                                                    false,
                                                    apiName
                                                )
                                            )

                                        }
                                        /*EDIT_CHARACTER->{
                                            val responseBody = response.body() as CharactersResponseModel
                                            responseBody.apiName = EDIT_CHARACTER
                                            characterResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(false)
                                        }*/
                                    }
                                }
                                else -> {
                                    if (apiName != DELETE_POST)
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
                            if (apiName != DELETE_POST)
                                updateLoaderStatus.postValue(LoadingData(false, apiName))
                            parseError(
                                response.errorBody(), response.code(),
                                apiName
                            )
                        }
                    }
                } catch (e: Exception) {
                    catchException(e, apiName)
                    if (apiName != DELETE_POST)
                        updateLoaderStatus.postValue(LoadingData(false, apiName))
                }
            }
        } else
            exceptions.postValue(ExceptionData(context.getString(R.string.no_internet), apiName))
    }

    fun addEditCharacter(
        addCharacter: Boolean,
        file: MultipartBody.Part?,
        requestPart: MutableMap<String, RequestBody>,
        context: Context
    ) {
        if (context.isInternetAvailable()) {
            val apiName = if (addCharacter) ADD_NEW_CHARACTER else EDIT_CHARACTER
            updateLoaderStatus.postValue(LoadingData(true, apiName))
            job = scope.launch {
                try {
                    val response =
                        if (addCharacter)
                            characterRepository.addNewCharacter(requestPart, file)
                        else
                            characterRepository.editCharacter(requestPart, file)
                    when (response.isSuccessful) {
                        true -> {
                            when (response.code()) {
                                200, 201 -> {
                                    val responseBody = response.body() as CharactersResponseModel
                                    responseBody?.apiName = apiName
                                    characterResponseResult.postValue(responseBody)
                                    updateLoaderStatus.postValue(LoadingData(false, apiName))
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
            exceptions.postValue(
                ExceptionData(
                    context.getString(R.string.no_internet),
                    ADD_NEW_CHARACTER
                )
            )
    }
}