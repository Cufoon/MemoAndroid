package cufoon.memo.android.data.repository

import com.skydoves.sandwich.ApiResponse
import cufoon.memo.android.data.api.MemosApiService
import cufoon.memo.android.data.model.Resource
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject


class ResourceRepository @Inject constructor(private val memosApiService: MemosApiService) {
    suspend fun loadResources(): ApiResponse<List<Resource>> = memosApiService.call { api ->
        api.getResources()
    }

    suspend fun uploadResource(
        resourceData: ByteArray,
        filename: String,
        mediaType: MediaType
    ): ApiResponse<Resource> = memosApiService.call { api ->
        val file = MultipartBody.Part.createFormData(
            "file",
            filename,
            resourceData.toRequestBody(mediaType)
        )
        api.uploadResource(file)
    }

    suspend fun deleteResource(resourceId: Long): ApiResponse<Unit> = memosApiService.call { api ->
        api.deleteResource(resourceId)
    }
}