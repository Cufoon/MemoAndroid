package cufoon.memo.android.data.model

import kotlinx.serialization.Serializable


@Serializable
data class ErrorMessage(
    val error: String? = null,
    val message: String
)
