package cufoon.memo.android.data.constant

import cufoon.memo.android.R
import cufoon.memo.android.ext.string


class MoeMemosException(string: String) : Exception(string) {
    companion object {
        val notLogin = MoeMemosException("NOT_LOGIN")
        val invalidOpenAPI = MoeMemosException("INVALID_OPEN_API")
    }

    override fun getLocalizedMessage(): String? {
        return when (this) {
            invalidOpenAPI -> R.string.invalid_open_api.string
            else -> {
                super.getLocalizedMessage()
            }
        }
    }
}