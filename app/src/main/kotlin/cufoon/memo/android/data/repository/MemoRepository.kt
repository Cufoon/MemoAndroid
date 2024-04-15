package cufoon.memo.android.data.repository

import com.skydoves.sandwich.ApiResponse
import cufoon.memo.android.data.api.CreateMemoInput
import cufoon.memo.android.data.api.DeleteTagInput
import cufoon.memo.android.data.api.MemosApiService
import cufoon.memo.android.data.api.PatchMemoInput
import cufoon.memo.android.data.api.UpdateMemoOrganizerInput
import cufoon.memo.android.data.api.UpdateTagInput
import cufoon.memo.android.data.model.Memo
import cufoon.memo.android.data.model.MemosRowStatus
import cufoon.memo.android.data.model.MemosVisibility
import javax.inject.Inject


class MemoRepository @Inject constructor(private val memosApiService: MemosApiService) {
    suspend fun loadMemos(rowStatus: MemosRowStatus? = null): ApiResponse<List<Memo>> =
        memosApiService.call { api ->
            api.listMemo(rowStatus = rowStatus)
        }

    suspend fun createMemo(
        content: String,
        resourceIdList: List<Long>? = null,
        visibility: MemosVisibility = MemosVisibility.PRIVATE
    ): ApiResponse<Memo> = memosApiService.call { api ->
        api.createMemo(
            CreateMemoInput(
                content,
                resourceIdList = resourceIdList,
                visibility = visibility
            )
        )
    }

    suspend fun getTags(): ApiResponse<List<String>> = memosApiService.call { api ->
        api.getTags()
    }

    suspend fun updateTag(name: String): ApiResponse<String> = memosApiService.call { api ->
        api.updateTag(UpdateTagInput(name))
    }

    suspend fun deleteTag(name: String): ApiResponse<Unit> = memosApiService.call { api ->
        api.deleteTag(DeleteTagInput(name))
    }

    suspend fun updatePinned(memoId: Long, pinned: Boolean): ApiResponse<Memo> =
        memosApiService.call { api ->
            api.updateMemoOrganizer(memoId, UpdateMemoOrganizerInput(pinned = pinned))
        }

    suspend fun archiveMemo(memoId: Long): ApiResponse<Memo> = memosApiService.call { api ->
        api.patchMemo(memoId, PatchMemoInput(id = memoId, rowStatus = MemosRowStatus.ARCHIVED))
    }

    suspend fun restoreMemo(memoId: Long): ApiResponse<Memo> = memosApiService.call { api ->
        api.patchMemo(memoId, PatchMemoInput(id = memoId, rowStatus = MemosRowStatus.NORMAL))
    }

    suspend fun deleteMemo(memoId: Long): ApiResponse<Unit> = memosApiService.call { api ->
        api.deleteMemo(memoId)
    }

    suspend fun editMemo(
        memoId: Long,
        content: String,
        resourceIdList: List<Long>? = null,
        visibility: MemosVisibility = MemosVisibility.PRIVATE
    ): ApiResponse<Memo> = memosApiService.call { api ->
        api.patchMemo(
            memoId,
            PatchMemoInput(
                id = memoId,
                content = content,
                resourceIdList = resourceIdList,
                visibility = visibility
            )
        )
    }

    suspend fun listAllMemo(
        limit: Int? = null,
        offset: Int? = null,
        pinned: Boolean? = null,
        tag: String? = null,
        visibility: MemosVisibility? = null
    ): ApiResponse<List<Memo>> = memosApiService.call { api ->
        api.listAllMemo(
            limit = limit,
            offset = offset,
            pinned = pinned,
            tag = tag,
            visibility = visibility
        )
    }
}
