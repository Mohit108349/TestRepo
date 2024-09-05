package com.familypedia.repository

import com.familypedia.network.*
import retrofit2.Response

object PermissionRepository {
    private var repository: PermissionRepository? = null
    private lateinit var api: FamilyPediaServices
    val instance: PermissionRepository
        get() {
            repository = PermissionRepository
            api = ServiceGenerator.createService(FamilyPediaServices::class.java)
            return repository!!
        }

    suspend fun askPermission(request: AskCharacterPermissionRequest): Response<SimpleResponseModel> {
        return api.requestCharacterPermission(request)
    }

    suspend fun getPermissionsList(page: Int): Response<PermissionResponse> {
        return api.getCharactersPermissionList(page)
    }

    suspend fun acceptDeclineCharacterPermission(
        accept: Boolean,
        request: AskCharacterPermissionRequest
    ): Response<SimpleResponseModel> {
        return if (accept)
            api.acceptCharacterPermission(request)
        else
            api.declineCharacterPermission(request)
    }

    suspend fun removeGivenCharacterPermission(
        request: AskCharacterPermissionRequest
    ): Response<SimpleResponseModel> {
        return api.removeGivenCharacterPermission(request)
    }

}