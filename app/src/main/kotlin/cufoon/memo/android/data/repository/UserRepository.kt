package cufoon.memo.android.data.repository

import com.skydoves.sandwich.ApiResponse
import cufoon.memo.android.data.api.MemosApiService
import cufoon.memo.android.data.model.User
import javax.inject.Inject


class UserRepository @Inject constructor(private val memosApiService: MemosApiService) {
    suspend fun getCurrentUser(): ApiResponse<User> = memosApiService.call { api ->
        api.me()
    }
}