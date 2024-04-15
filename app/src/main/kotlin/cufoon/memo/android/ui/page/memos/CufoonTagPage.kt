package cufoon.memo.android.ui.page.memos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cufoon.memo.android.R
import cufoon.memo.android.ext.string
import cufoon.memo.android.ui.component.EmptyCard
import cufoon.memo.android.ui.component.TagListItem
import cufoon.memo.android.viewmodel.LocalMemos


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CufoonTagPage(rootNC: NavHostController, outerPaddingValues: PaddingValues) {
    val memosViewModel = LocalMemos.current

    Scaffold(modifier = Modifier.padding(outerPaddingValues),
        topBar = { TopAppBar(title = { Text(text = R.string.tags.string) }) }) { innerPadding ->
        val tagList = memosViewModel.tags.toList()
        if (tagList.isEmpty()) {
            EmptyCard(Modifier.padding(innerPadding), description = "暂无任何标签")
        } else {
            FlowRow(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                verticalArrangement = Arrangement.Top
            ) {
                tagList.forEach { tag ->
                    TagListItem(rootNC, tag)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        memosViewModel.loadTags()
    }
}