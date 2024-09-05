package com.familypedia.repository

import com.familypedia.network.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

object CharacterRepository {
    private var repository: CharacterRepository? = null
    private lateinit var api: FamilyPediaServices
    val instance: CharacterRepository
        get() {
            repository = CharacterRepository
            api = ServiceGenerator.createService(FamilyPediaServices::class.java)
            return repository!!
        }

    suspend fun addNewCharacter(
        params: Map<String, RequestBody>,
        file: MultipartBody.Part?
    ): Response<CharactersResponseModel> {

        return api.addNewCharacter(file, params)
    }

    suspend fun getCharactersByUser(request: SearchRequest): Response<CharactersListResponseModel> {
        return api.getCharacterByUser(request.page,request.searchText)
    }
    suspend fun getMyCharacterAndPermittedCharactersList(request: SearchRequest): Response<CharactersListResponseModel> {
        return api.getMyCharacterAndPermittedCharactersList(request.page,request.searchText)
    }

    suspend fun getCharactersWithPermissionsUser(page: Int): Response<CharactersListResponseModel> {
        return api.getCharacterWithPermissionUser(page)
    }

    suspend fun getCharactersProfile(characterId: String): Response<CharactersResponseModel> {
        return api.getCharacterProfile(characterId)
    }

    suspend fun editCharacter(
        params: Map<String, RequestBody>,
        file: MultipartBody.Part?
    ): Response<CharactersResponseModel> {
        return api.editCharacter(file, params)
    }


    suspend fun createUpdatePost(
        createPostRequest: CreatePostRequest
    ): Response<CreatePostResponseModel> {
        val imageList = ArrayList<MultipartBody.Part?>()
        if (!createPostRequest.imageList.isNullOrEmpty()) {
            for (item in createPostRequest.imageList) {
                if (!item.startsWith("profile_pictures")) {
                    val file = File(item)
                    val reqFile = file.asRequestBody("image/jpg".toMediaTypeOrNull())
                    imageList.add(
                        MultipartBody.Part.createFormData(
                            "attachPhotos",
                            file.name,
                            reqFile
                        )
                    )
                }
            }
        }
        val requestPart: MutableMap<String, RequestBody> = HashMap()
        requestPart["characterId"] = createPostRequest.characterId.toRequestBody()
        requestPart["startingDate"] = createPostRequest.startingDate.toRequestBody()
        requestPart["startingDateTimeStamp"] = createPostRequest.startingDateTimeStamp.toRequestBody()
        if (createPostRequest.endingDate.isNotEmpty()) {
            requestPart["endingDate"] = createPostRequest.endingDate.toRequestBody()
        }
        requestPart["eventType"] = createPostRequest.eventType.toRequestBody()
        requestPart["description"] = createPostRequest.description.toRequestBody()
        requestPart["location"] = createPostRequest.location.toRequestBody()
        if (createPostRequest.postId != null && createPostRequest.postId.isNotEmpty()) {
            requestPart["postId"] = createPostRequest.postId.toRequestBody()
        }

        if (createPostRequest.postId != null && createPostRequest.postId.isNotEmpty()) {
            return api.updatePost(imageList, requestPart)
        }
        return api.createPost(imageList, requestPart)
    }

    suspend fun getHomePosts(request: SearchRequest): Response<PostListResponse> {
        /*if (request?.page==-1){
            return api.getCharacterPosts(request.searchText)
        }else {
        */
        return api.getHomePosts(request?.searchText.toString(), request?.page!!)
        //}
    }

    suspend fun getTimeLinePosts(characterId: String): Response<TimelineListResponse> {
        return api.getCharacterPosts(characterId)
    }

    suspend fun deletePost(postId: String): Response<SimpleResponseModel> {
        return api.deletePost(postId)
    }

    suspend fun reportPost(reportPostRequest: ReportPostRequest): Response<SimpleResponseModel> {
        return api.reportPost(reportPostRequest)
    }

    suspend fun searchCharacter(request: SearchRequest): Response<CharactersListResponseModel> {
        return api.searchCharacter(request.searchText, /*"",*/request.page)
    }

    suspend fun addRemoveBookmark(request: BookmarkRequest): Response<SimpleResponseModel> {
        return api.addRemoveBookmark(request)
    }
    suspend fun deleteUserBio(id:String): Response<SimpleResponseModel> {
        return api.deleteUserBio(id)
    }

    suspend fun getBookmarkList(page: SearchRequest): Response<CharactersListResponseModel> {
        return api.getBookmarkList(page.searchText, page.page)
    }
    suspend fun getCharacterByAnotherUser(request:UserRequest): Response<CharactersListResponseModel> {
        return api.getCharacterByAnotherUser(request.userId,request.page)
    }

    suspend fun removePostImage(request:RemovePostImageRequest):Response<SimpleResponseModel>{
        return api.removePostPhotos(request.postId,request.index)
    }

    suspend fun getPostDetails(postId:String):Response<CreatePostResponseModel>{
        return api.getPostDetail(postId)
    }



}