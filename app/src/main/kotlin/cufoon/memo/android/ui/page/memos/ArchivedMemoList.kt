package cufoon.memo.android.ui.page.memos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cufoon.memo.android.R
import cufoon.memo.android.ext.string
import cufoon.memo.android.ui.component.ArchivedMemoCard
import cufoon.memo.android.ui.component.EmptyCard
import cufoon.memo.android.ui.component.LoadingIndicator
import cufoon.memo.android.viewmodel.ArchivedMemoListViewModel
import cufoon.memo.android.viewmodel.LocalArchivedMemos
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedMemoList(
    viewModel: ArchivedMemoListViewModel = hiltViewModel(),
    outerPadding: PaddingValues
) {
    val layoutDirection = LocalLayoutDirection.current
    val lazyListState = rememberLazyStaggeredGridState()
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalArchivedMemos provides viewModel) {
        Scaffold(modifier = Modifier.padding(outerPadding),
            topBar = { TopAppBar(title = { Text(text = R.string.archived.string) }) }) { innerPadding ->

            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(360.dp),
                modifier = Modifier
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize(),
                contentPadding = innerPadding.let {
                    PaddingValues(
                        it.calculateStartPadding(layoutDirection) + 16.dp,
                        it.calculateTopPadding(),
                        it.calculateEndPadding(layoutDirection) + 16.dp,
                        it.calculateBottomPadding() + 96.dp
                    )
                },
                state = lazyListState,
                verticalItemSpacing = 16.dp,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isInitialized) {
                    if (viewModel.memos.isEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            if (isLoading) {
                                LoadingIndicator()
                            } else {
                                EmptyCard(Modifier.padding(innerPadding), "暂无归档笔记")
                            }
                        }
                    } else {
                        items(viewModel.memos, key = { it.id }, span = {
                            if (it.pinned) StaggeredGridItemSpan.FullLine else StaggeredGridItemSpan.SingleLane
                        }) { memo ->
                            ArchivedMemoCard(memo)
                        }
                    }
                }
            }
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
}