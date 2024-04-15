package cufoon.memo.android.ext

import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.message
import com.skydoves.sandwich.retrofit.serialization.deserializeErrorBody
import com.skydoves.sandwich.retrofit.statusCode
import com.skydoves.sandwich.suspendOnError
import cufoon.memo.android.data.constant.MoeMemosException
import cufoon.memo.android.data.model.ErrorMessage
import cufoon.memo.android.util.console


fun <T> ApiResponse<T>.getErrorMessage(): String {
    if (this is ApiResponse.Failure.Error) {
        try {
            val errorMessage: ErrorMessage? = this.deserializeErrorBody()
            if (errorMessage != null) {
                return errorMessage.message
            }
        } catch (e: Throwable) {
            console.log("getErrorMessage $e")
        }
        return this.message()
    }

    if (this is ApiResponse.Failure.Exception) {
        return this.message()
    }
    return ""
}

suspend inline fun <T> ApiResponse<T>.suspendOnErrorMessage(crossinline block: suspend (message: String) -> Unit): ApiResponse<T> {
    if (this is ApiResponse.Failure.Error) {
        block(getErrorMessage())
    } else if (this is ApiResponse.Failure.Exception) {
        block(getErrorMessage())
    }

    return this
}

suspend inline fun <T> ApiResponse<T>.suspendOnNotLogin(crossinline block: suspend ApiResponse<T>.() -> Unit): ApiResponse<T> {
    console.log("hahaha $this")
    if (this.toString() == MoeMemosException.notLogin.localizedMessage) {
        console.log("MoeMemosException.notLogin")
        block(this@suspendOnNotLogin)
        return this
    }
    this.suspendOnError {
        if (this.statusCode.code == 401) {
            console.log("401 Unauthorized")
            block(this@suspendOnNotLogin)
        }
    }
    return this
}