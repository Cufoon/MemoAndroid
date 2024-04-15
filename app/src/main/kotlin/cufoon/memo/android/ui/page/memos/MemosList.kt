package cufoon.memo.android.ui.page.memos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cufoon.memo.android.ui.component.EmptyCard
import cufoon.memo.android.ui.component.LoadingIndicator
import cufoon.memo.android.ui.component.MemosCard
import cufoon.memo.android.util.console
import cufoon.memo.android.viewmodel.LocalMemos
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MemosList(
    outerPadding: PaddingValues,
    swipeEnabled: Boolean = true,
    tag: String? = null,
    searchString: String? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val viewModel = LocalMemos.current
    val refreshState = rememberPullRefreshState(viewModel.refreshing, onRefresh = {
        coroutineScope.launch {
            viewModel.refresh()
        }
    })
    var isLoading by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }
    val filteredMemos = remember(viewModel.memos.toList(), tag, searchString) {
        val pinned = viewModel.memos.filter { it.pinned }
        val nonPinned = viewModel.memos.filter { !it.pinned }
        var fullList = pinned + nonPinned

        tag?.let { tag ->
            fullList = fullList.filter { memo ->
                memo.content.contains("#$tag") || memo.content.contains("#$tag/")
            }
        }

        searchString?.let { searchString ->
            if (searchString.isNotEmpty()) {
                fullList = fullList.filter { memo ->
                    memo.content.contains(searchString, true)
                }
            }
        }

        fullList
    }

    val lazyListState = rememberLazyStaggeredGridState()
    var listTopId: Long? by rememberSaveable {
        mutableStateOf(null)
    }

    Box(Modifier.pullRefresh(refreshState, swipeEnabled)) {
        //val cells = floor(
        //    (maxWidth.value - 32 - outerPadding.calculateStartPadding(layoutDirection).value - outerPadding.calculateEndPadding(
        //        layoutDirection
        //    ).value + 12) / 312f
        //).toInt()
        //val importantSpan = if (cells > 1) {
        //    val rare = cells % 3
        //    if (rare == 2) {
        //        2
        //    } else 3
        //} else 1
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(300.dp),
            modifier = Modifier.padding(outerPadding),
            contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 96.dp),
            state = lazyListState,
            verticalItemSpacing = 16.dp,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isInitialized) {
                if (filteredMemos.isEmpty()) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        if (isLoading) {
                            LoadingIndicator()
                        } else {
                            EmptyCard(description = "暂无笔记")
                        }
                    }
                } else {
                    items(filteredMemos, key = { it.id }, span = {
                        if (it.pinned) StaggeredGridItemSpan.FullLine else StaggeredGridItemSpan.SingleLane
                    }) { memo ->
                        MemosCard(memo, previewMode = true)
                    }
                }
            }
        }
        PullRefreshIndicator(
            viewModel.refreshing,
            refreshState,
            Modifier
                .align(Alignment.TopCenter)
                .padding(outerPadding)
        )
    }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            console.log("MemosList $it")
        }
    }

    LaunchedEffect(Unit) {
        val jobForStartLoading = coroutineScope.launch {
            delay(100)
            isLoading = true
            isInitialized = true
        }
        viewModel.loadMemos().invokeOnCompletion {
            jobForStartLoading.cancel()
            coroutineScope.launch {
                delay(200)
                isLoading = false
                isInitialized = true
            }
        }
    }

    LaunchedEffect(filteredMemos.firstOrNull()?.id) {
        if (listTopId != null && filteredMemos.isNotEmpty() && listTopId != filteredMemos.first().id) {
            lazyListState.scrollToItem(0)
        }

        listTopId = filteredMemos.firstOrNull()?.id
    }
}