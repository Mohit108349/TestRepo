package com.familypedia.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.familypedia.FamilyPediaApplication
import com.familypedia.R
import com.familypedia.network.ErrorData
import com.familypedia.network.ExceptionData
import com.familypedia.network.LoadingData
import com.familypedia.utils.ErrorResponse
import com.familypedia.utils.NoConnectivityException
import com.familypedia.utils.Utility.unAuthorizedInactiveUser
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException

open class BaseViewModel(application: Application) : AndroidViewModel(application) {
    private val errors = MutableLiveData<ErrorData>()
    val error: LiveData<ErrorData> = errors
    val exceptions = MutableLiveData<ExceptionData>()
    val exception: LiveData<ExceptionData> = exceptions
    var updateLoaderStatus = MutableLiveData<LoadingData>()
    var updateLoaderStatusLoadMore = MutableLiveData<Boolean>()
    private var exceptionMessage = ""
    private val somethingWentWrong = "Something Went Wrong"
    var exceptionData: ExceptionData? = null
    fun catchException(exception: Exception, apiName: String) {
        exceptionMessage = if (exception.localizedMessage == "timeout") {
            FamilyPediaApplication.instance.getString(R.string.server_timeout)
        } else {
            exception.localizedMessage ?: somethingWentWrong
        }
        exceptionData = ExceptionData(exceptionMessage, apiName)
        when (exception) {
            is HttpException -> {
                exceptions.postValue(exceptionData)
            }
            is NoConnectivityException -> {
                exceptionData = ExceptionData("No Internet Connection", apiName)
                exceptions.postValue(exceptionData)
            }
            is SocketTimeoutException -> {
                exceptionData = ExceptionData(FamilyPediaApplication.instance.getString(R.string.server_timeout), apiName)
                exceptions.postValue(exceptionData)
            }
            is  ConnectException ->{
                exceptionData = ExceptionData(FamilyPediaApplication.instance.getString(R.string.couldnt_connect), apiName)
                exceptions.postValue(exceptionData)
            }

            else -> {
                exceptions.postValue(exceptionData)
            }
        }
    }

    fun parseError(responseBody: ResponseBody?, code: Int, apiName: String) {
        val errorResponse: ErrorResponse = Gson().fromJson(
            responseBody?.charStream(),
            ErrorResponse::class.java
        )
        val errorData = ErrorData(errorResponse.message, apiName, code)
        when (code) {
            401 -> {
                //Utility.unAuthorizedUser()
                //unAuthorizedInactiveUser(getApplication(),errorData.message)
                //errors.postValue(ErrorData("UnAuthorized User",apiName,code))
            }
            /*500 -> {
                errors.postValue(ErrorData("Internal Sever Error",apiName,code))
            }
            404 -> {
                errors.postValue(ErrorData("API URL Not Found",apiName,code))
            }*/
            else -> errors.postValue(errorData)
        }
        errors.postValue(errorData)
    }
}