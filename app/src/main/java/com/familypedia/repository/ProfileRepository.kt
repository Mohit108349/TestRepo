package com.familypedia.repository

import com.familypedia.network.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

object ProfileRepository {
    private var repository: ProfileRepository? = null
    private lateinit var api: FamilyPediaServices
    val instance: ProfileRepository
        get() {
            repository = ProfileRepository
            api = ServiceGenerator.createService(FamilyPediaServices::class.java)
            return repository!!
        }

    suspend fun updateProfile(
        params: Map<String, RequestBody>,
        file: MultipartBody.Part?
    ): Response<ProfileResponseModel> {
        return api.updateProfile(file, params)
    }
    suspend fun sendOTP(request: ResendOtpRequest): Response<ProfileResponseModel> {
        return api.sendOTP(request)
    }

    suspend fun verifyAuthType(
        resendOtpRequest: ResendOtpRequest,
    ): Response<AuthResponseModel> {
        return api.resendOTP(resendOtpRequest)
    }

}