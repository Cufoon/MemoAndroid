package cufoon.memo.android.data.model

import androidx.annotation.Keep


@Keep
data class Profile(
    val mode: String,
    val version: String
)

@Keep
data class Status(
    val profile: Profile
)