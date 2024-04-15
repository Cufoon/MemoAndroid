package cufoon.memo.android.ui.page.resource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cufoon.memo.android.R
import cufoon.memo.android.ext.string
import cufoon.memo.android.ui.component.EmptyCard
import cufoon.memo.android.ui.component.LoadingIndicator
import cufoon.memo.android.ui.component.MemoImage
import cufoon.memo.android.viewmodel.LocalUserState
import cufoon.memo.android.viewmodel.ResourceListViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CufoonResourceListPage(
    outerPaddingValues: PaddingValues, viewModel: ResourceListViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.padding(outerPaddingValues),
        topBar = { TopAppBar(title = { Text(text = R.string.resources.string) }) }) { innerPadding ->
        if (isInitialized) {
            if (viewModel.resources.isEmpty()) {
                Row(Modifier.fillMaxWidth()) {
                    if (isLoading) {
                        LoadingIndicator()
                    } else {
                        EmptyCard(Modifier.padding(innerPadding), description = "暂无任何资源")
                    }
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(150.dp),
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp,
                    userScrollEnabled = true
                ) {
                    items(viewModel.resources, key = { it.id }) { resource ->
                        MemoImage(
                            url = resource.uri(LocalUserState.current.host).toString(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val jobForStartLoading = coroutineScope.launch {
            isLoading = true
            isInitialized = true
        }
        viewModel.loadResources().invokeOnCompletion {
            jobForStartLoading.cancel()
            coroutineScope.launch {
                isLoading = false
                isInitialized = true
            }
        }
    }
}