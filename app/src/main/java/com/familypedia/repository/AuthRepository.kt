package com.familypedia.repository

import com.familypedia.network.*
import retrofit2.Response

object AuthRepository {
    private var repository: AuthRepository? = null
    private lateinit var api: FamilyPediaServices
    val instance: AuthRepository
        get() {
            repository = AuthRepository
            api = ServiceGenerator.createService(FamilyPediaServices::class.java)
            return repository!!
        }

    suspend fun userLogin(request: LoginRequest): Response<AuthResponseModel> {
        return api.userLogin(request)
    }


    /*suspend fun userSignup(request: SignupRequest): Response<AuthResponseModel> {
        return api.userSignup(request)
    }*/
    suspend fun userSignup(request: SignupRequestNew): Response<AuthResponseModel> {
        return api.userSignupNew(request)
    }
    suspend fun verifyOtp(email: String?, countryCode: String?, phone: String?, otp: String? ): Response<AuthResponseModel> {
        return api.userVerifyOtp(email = email, countryCode = countryCode, phoneNumber =  phone, otp = otp)
    }

    suspend fun forgotPassword(request: ForgotPasswordRequest): Response<AuthResponseModel> {
        return api.forgotPassword(request)
    }

    suspend fun resendOTP(request: ResendOtpRequest): Response<AuthResponseModel> {
        return api.resendOTP(request)
    }


    suspend fun logout(): Response<SimpleResponseModel> {
        return api.logout()
    }

    suspend fun deviceInfo(request: DeviceInfoRequest): Response<SimpleResponseModel> {
        return api.deviceInfo(request)
    }
    suspend fun userActive(request:String): Response<Any> {
        return api.userActive()
    }

    suspend fun changePassword(request: ChangePasswordRequest): Response<SimpleResponseModel> {
        return api.changePassword(request)
    }

    suspend fun resetPassword(request: ForgotPasswordRequest): Response<SimpleResponseModel> {
        return api.resetPassword(request)
    }

    suspend fun verifyAccount(email: EmailRequest): Response<AuthResponseModel> {
        return api.verifyAccount(email.email)
    }

    suspend fun resendSignupLink(request: ResendSignupLinkRequest): Response<SimpleResponseModel> {
        return api.resendSignupLink(request)
    }

    suspend fun deleteUserAccount(id:String): Response<SimpleResponseModel> {
        return api.deleteUserAccount(id)
    }


    suspend fun verifyPassword(req : VerifyPassword): Response<SimpleResponseModel> {
        return api.verifyPassword(req)
    }



}