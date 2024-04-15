package cufoon.memo.android.ui.page.memos

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import cufoon.memo.android.R
import cufoon.memo.android.ext.string
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CufoonTagMemoPage(
    rootNC: NavHostController,
    tag: String
) {
    val scope = rememberCoroutineScope()

    Scaffold(topBar = {
        TopAppBar(title = { Text(tag) }, navigationIcon = {
            IconButton(onClick = { scope.launch { rootNC.navigateUp() } }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = R.string.menu.string)
            }
        })
    }) { innerPadding ->
        MemosList(
            outerPadding = innerPadding, tag = tag
        )
    }
}