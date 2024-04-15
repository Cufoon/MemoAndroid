package cufoon.memo.android.viewmodel

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.suspendOnSuccess
import cufoon.memo.android.data.model.DailyUsageStat
import cufoon.memo.android.data.model.Memo
import cufoon.memo.android.data.model.MemosRowStatus
import cufoon.memo.android.data.model.MemosVisibility
import cufoon.memo.android.data.model.Resource
import cufoon.memo.android.data.repository.MemoRepository
import cufoon.memo.android.ext.string
import cufoon.memo.android.ext.suspendOnErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import javax.inject.Inject


@HiltViewModel
class MemosViewModel @Inject constructor(
    private val memoRepository: MemoRepository
) : ViewModel() {

    var memos = mutableStateListOf<Memo>()
        private set
    var tags = mutableStateListOf<String>()
        private set
    var errorMessage: String? by mutableStateOf(null)
        private set
    var refreshing by mutableStateOf(false)
        private set
    var matrix by mutableStateOf(DailyUsageStat.initialMatrix)
        private set

    init {
        snapshotFlow { memos.toList() }.onEach { matrix = calculateMatrix() }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        refreshing = true
        loadMemos().invokeOnCompletion {
            refreshing = false
        }
    }

    fun loadMemos() = viewModelScope.launch {
        memoRepository.loadMemos(rowStatus = MemosRowStatus.NORMAL).suspendOnSuccess {
            memos.clear()
            memos.addAll(data)
            errorMessage = null
        }.suspendOnErrorMessage {
            errorMessage = it
        }
    }

    fun loadTags() = viewModelScope.launch {
        memoRepository.getTags().suspendOnSuccess {
            tags.clear()
            tags.addAll(data)
        }
    }

    suspend fun deleteTag(name: String) = withContext(viewModelScope.coroutineContext) {
        memoRepository.deleteTag(name).suspendOnSuccess {
            tags.remove(name)
        }
    }

    suspend fun updateMemoPinned(memoId: Long, pinned: Boolean) =
        withContext(viewModelScope.coroutineContext) {
            memoRepository.updatePinned(memoId, pinned).suspendOnSuccess {
                updateMemo(data)
            }
        }

    suspend fun editMemo(
        memoId: Long, content: String, resourceList: List<Resource>?, visibility: MemosVisibility
    ): ApiResponse<Memo> = withContext(viewModelScope.coroutineContext) {
        memoRepository.editMemo(memoId, content, resourceList?.map { it.id }, visibility)
            .suspendOnSuccess {
                updateMemo(data)
            }
    }

    suspend fun archiveMemo(memoId: Long) = withContext(viewModelScope.coroutineContext) {
        memoRepository.archiveMemo(memoId).suspendOnSuccess {
            memos.removeIf { it.id == memoId }
        }
    }

    private fun updateMemo(memo: Memo) {
        val index = memos.indexOfFirst { it.id == memo.id }
        if (index != -1) {
            memos[index] = memo
        }
    }

    private fun calculateMatrix(): List<DailyUsageStat> {
        val countMap = HashMap<LocalDate, Int>()

        for (memo in memos) {
            val date = LocalDateTime.ofEpochSecond(memo.createdTs, 0, OffsetDateTime.now().offset)
                .toLocalDate()
            countMap[date] = (countMap[date] ?: 0) + 1
        }

        return DailyUsageStat.initialMatrix.map {
            it.copy(count = countMap[it.date] ?: 0)
        }
    }
}

val LocalMemos =
    compositionLocalOf<MemosViewModel> { error(cufoon.memo.android.R.string.memos_view_model_not_found.string) }