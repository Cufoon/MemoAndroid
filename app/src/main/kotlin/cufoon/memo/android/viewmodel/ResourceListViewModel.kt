package cufoon.memo.android.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skydoves.sandwich.suspendOnSuccess
import cufoon.memo.android.data.model.Resource
import cufoon.memo.android.data.repository.ResourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ResourceListViewModel @Inject constructor(
    private val resourceRepository: ResourceRepository
) : ViewModel() {
    var resources = mutableStateListOf<Resource>()
        private set

    fun loadResources() = viewModelScope.launch {
        resourceRepository.loadResources().suspendOnSuccess {
            resources.clear()
            resources.addAll(data.filter { it.type.startsWith("image/") })
        }
    }
}