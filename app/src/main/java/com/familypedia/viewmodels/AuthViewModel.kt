package com.familypedia.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.familypedia.R
import com.familypedia.network.*
import com.familypedia.repository.AuthRepository
import com.familypedia.repository.AuthRepository.verifyOtp
import com.familypedia.utils.isInternetAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Response
import kotlin.math.sign

class AuthViewModel(application: Application) : BaseViewModel(application = application) {
    lateinit var authRepository: AuthRepository
    val scope = CoroutineScope(Dispatchers.IO)
    var job: Job? = null
    var authResponseResult = MutableLiveData<AuthResponseModel>()
    var simpleResponseResult = MutableLiveData<SimpleResponseModel>()
    fun init() {
        authRepository = AuthRepository.instance
    }

    fun signupToTheApplication(
        context: Context,
        signupRequest: SignupRequestNew
    ) {
        val valueToShow = if (signupRequest.type == "email")
         signupRequest.email
        else
        signupRequest.phoneNumber

        hitAPI(context, signupRequest, SIGNUP,valueToShow?:"")
    }

    fun loginToTheApplication(
        context: Context,
        loginRequest: LoginRequest
    ) {
        hitAPI(context, loginRequest, LOGIN)
    }

    fun forgotPassword(
        context: Context,
        request: ForgotPasswordRequest
    ) {
        hitAPI(context, request, FORGOT_PASSWORD)
    }

    fun resendOtpAPI(
        context: Context,
        request: ResendOtpRequest
    ) {
        hitAPI(context, request, RESEND_OTP)
    }
    fun verifyOtpAPI(
        context: Context,
        request: VerifyOTPRequest
    ) {
        hitAPI(context, request, VERIFY_OTP)
    }

    fun logoutAPI(
        context: Context
    ) {
        hitAPI(context, null, LOGOUT)
    }

    fun verifyPassword(
        context: Context,
        request: VerifyPassword
    ) {
        hitAPI(context, request, VERIFY_PASSWORD)
    }

    fun deviceInfoAPI(
        request: DeviceInfoRequest,
        context: Context
    ) {
        hitAPI(context, request, DEVICE_INFO)
    }
    fun userActiveAPI(
        request:String,
        context: Context
    ) {
        hitAPI(context, request, USER_ACTIVE)
    }

    fun deleteUserAccount(
        context: Context,
        id:String
        ) {
        hitAPI(context, id, DELETE_USER)
    }

    fun changePasswordAPI(
        request: ChangePasswordRequest,
        context: Context
    ) {
        hitAPI(context, request, CHANGE_PASSWORD)
    }

    fun resetPasswordAPI(request: ForgotPasswordRequest, context: Context) {
        hitAPI(context, request, RESET_PASSWORD)
    }

    fun verifyAccount(emailRequest: EmailRequest, context: Context) {
        hitAPI(context, emailRequest, VERIFY_ACCOUNT)
    }

    fun resendSignupVerificationLink(request: ResendSignupLinkRequest, context: Context) {
        hitAPI(context, request, RESEND_SIGNUP)
    }


    private fun hitAPI(context: Context, request: Any?, apiName: String,value:String="") {
        if (context.isInternetAvailable()) {
            updateLoaderStatus.postValue(LoadingData(true, apiName))
            job = scope.launch {
                try {
                    var response: Response<Any>? = null
                    response = when (apiName) {
                        LOGIN -> authRepository.userLogin(request as LoginRequest) as Response<Any>
                        SIGNUP -> authRepository.userSignup(request as SignupRequestNew) as Response<Any>
                        FORGOT_PASSWORD -> authRepository.forgotPassword(request as ForgotPasswordRequest) as Response<Any>
                        RESEND_OTP -> authRepository.resendOTP(request as ResendOtpRequest) as Response<Any>
                        VERIFY_OTP -> {
                            val req = request as VerifyOTPRequest
                            authRepository.verifyOtp(email = req.email, countryCode = req.countryCode, phone = req.phoneNumber, otp = req.otp) as Response<Any>
                        }
                        LOGOUT -> authRepository.logout() as Response<Any>
                        DEVICE_INFO -> authRepository.deviceInfo(request as DeviceInfoRequest) as Response<Any>
                        USER_ACTIVE -> authRepository.userActive(request as String) as Response<Any>
                        CHANGE_PASSWORD -> authRepository.changePassword(request as ChangePasswordRequest) as Response<Any>
                        RESET_PASSWORD -> authRepository.resetPassword(request as ForgotPasswordRequest) as Response<Any>
                        VERIFY_ACCOUNT -> authRepository.verifyAccount(request as EmailRequest) as Response<Any>
                        RESEND_SIGNUP -> authRepository.resendSignupLink(request as ResendSignupLinkRequest) as Response<Any>


                        // For DELETE USER & Verify Password
                        DELETE_USER -> authRepository.deleteUserAccount(request as String) as Response<Any>
                        VERIFY_PASSWORD -> authRepository.verifyPassword(request as VerifyPassword) as Response<Any>

                        else -> return@launch
                    }
                    when (response?.isSuccessful) {
                        true -> {
                            when (response.code()) {
                                200, 201 -> {
                                    when (apiName) {
                                        LOGIN -> {
                                            val loginResponse = response.body() as AuthResponseModel
                                            loginResponse.apiName = apiName
                                            authResponseResult.postValue(loginResponse)
                                            updateLoaderStatus.postValue(
                                                LoadingData(
                                                    false,
                                                    apiName
                                                )
                                            )
                                        }
                                        SIGNUP -> {
                                            val signupResponse =
                                                response.body() as AuthResponseModel
                                            signupResponse.apiName = apiName
                                            signupResponse.value = value
                                            authResponseResult.postValue(signupResponse)
                                            updateLoaderStatus.postValue(
                                                LoadingData(
                                                    false,
                                                    apiName
                                                )
                                            )
                                        }
                                        FORGOT_PASSWORD -> {
                                            val responseBody = response.body() as AuthResponseModel
                                            responseBody.apiName = apiName
                                            authResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(
                                                    false,
                                                    apiName
                                                )
                                            )
                                        }
                                        RESEND_OTP -> {
                                            val responseBody = response.body() as AuthResponseModel
                                            responseBody.apiName = apiName
                                            authResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(
                                                    false,
                                                    apiName
                                                )
                                            )
                                        }
                                        LOGOUT -> {
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
                                        DEVICE_INFO -> {
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
                                        CHANGE_PASSWORD -> {
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
                                        RESET_PASSWORD -> {
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
                                        VERIFY_OTP -> {
                                            val responseBody = response.body() as AuthResponseModel
                                            responseBody.apiName = apiName
                                            authResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(
                                                    false,
                                                    apiName
                                                )
                                            )
                                        }
                                        VERIFY_ACCOUNT -> {
                                            val responseBody = response.body() as AuthResponseModel
                                            responseBody.apiName = apiName
                                            authResponseResult.postValue(responseBody)
                                            updateLoaderStatus.postValue(
                                                LoadingData(
                                                    false,
                                                    apiName
                                                )
                                            )
                                        }
                                        RESEND_SIGNUP -> {
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
                                        USER_ACTIVE -> {
                                            println("Success")
                                        }

                                        DELETE_USER -> {
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

                                        VERIFY_PASSWORD -> {
                                            val verifyPasswordResponse = response.body() as SimpleResponseModel
                                            verifyPasswordResponse.apiName = apiName
                                            simpleResponseResult.postValue(verifyPasswordResponse)
                                            updateLoaderStatus.postValue(
                                                LoadingData(
                                                    false,
                                                    apiName
                                                )
                                            )
                                        }
                                    }
                                }

                                else -> {
                                    updateLoaderStatus.postValue(LoadingData(false, apiName))
                                    parseError(
                                        response.errorBody(), response.code(),
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

                        else -> {}
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