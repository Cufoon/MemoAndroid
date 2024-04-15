package cufoon.memo.android.ui.page.memos

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cufoon.memo.android.R
import cufoon.memo.android.ext.string
import cufoon.memo.android.ui.page.common.RouteName


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CufoonMemosHomePage(rootNC: NavHostController, outerPaddingValues: PaddingValues) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(outerPaddingValues),
        bottomBar = { Row(Modifier.height(0.dp)) {} },
        topBar = {
            TopAppBar(title = { Text(text = R.string.memos.string) }, actions = {
                IconButton(onClick = {
                    rootNC.navigate(RouteName.SEARCH)
                }) {
                    Icon(Icons.Filled.Search, contentDescription = R.string.search.string)
                }
            })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { rootNC.navigate(RouteName.INPUT) },
                text = { Text(R.string.new_memo.string) },
                icon = {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = R.string.compose.string
                    )
                })
        }) { innerPadding ->
        MemosList(
            outerPadding = innerPadding
        )
    }
}
