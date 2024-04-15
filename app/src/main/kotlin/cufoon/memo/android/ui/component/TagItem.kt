package cufoon.memo.android.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.skydoves.sandwich.suspendOnSuccess
import cufoon.memo.android.R
import cufoon.memo.android.ext.string
import cufoon.memo.android.ui.page.common.RouteName
import cufoon.memo.android.viewmodel.LocalMemos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder


@Composable
fun TagListItem(
    rootNC: NavHostController,
    tag: String
) {
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val memosViewModel = LocalMemos.current

    Button(
        onClick = {
            scope.launch {
                rootNC.navigate("${RouteName.TAG}/${
                    withContext(Dispatchers.IO) {
                        URLEncoder.encode(tag, "UTF-8")
                    }
                }") {
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }, contentPadding = ButtonDefaults.ButtonWithIconContentPadding
    ) {
        Icon(
            Icons.Outlined.Tag,
            contentDescription = "a button of tag",
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(tag)
        Spacer(Modifier.size(ButtonDefaults.IconSpacing * 2))

        Icon(
            Icons.Outlined.Delete,
            contentDescription = "a button of delete",
            modifier = Modifier
                .clickable {
                    showDeleteDialog = true
                }
                .size(ButtonDefaults.IconSize)
        )
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(R.string.delete_this_tag.string) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            memosViewModel.deleteTag(tag).suspendOnSuccess {
                                showDeleteDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(R.string.confirm.string)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text(R.string.cancel.string)
                }
            }
        )
    }
}