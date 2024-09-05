
package com.familypedia.utils

import com.google.gson.annotations.SerializedName

data class ErrorResponse(@SerializedName("path")
                         val path: String = "",
                         @SerializedName("error")
                         val error: String = "",
                         @SerializedName("message")
                         val message: String = "",
                         @SerializedName("timestamp")
                         val timestamp: String = "",
                         @SerializedName("status")
                         val status: Int)