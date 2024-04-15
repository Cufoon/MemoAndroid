package cufoon.memo.android.ui.page.memoinput

import android.content.ActivityNotFoundException
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.skydoves.sandwich.suspendOnSuccess
import cufoon.memo.android.MoeMemosFileProvider
import cufoon.memo.android.R
import cufoon.memo.android.data.constant.LIST_ITEM_SYMBOL_LIST
import cufoon.memo.android.data.model.MemosVisibility
import cufoon.memo.android.data.model.ShareContent
import cufoon.memo.android.ext.icon
import cufoon.memo.android.ext.popBackStackIfLifecycleIsResumed
import cufoon.memo.android.ext.string
import cufoon.memo.android.ext.suspendOnErrorMessage
import cufoon.memo.android.ext.titleResource
import cufoon.memo.android.ui.component.Attachment
import cufoon.memo.android.ui.component.InputImage
import cufoon.memo.android.ui.page.common.LocalRootNavController
import cufoon.memo.android.util.console
import cufoon.memo.android.util.extractCustomTags
import cufoon.memo.android.viewmodel.LocalMemos
import cufoon.memo.android.viewmodel.LocalUserState
import cufoon.memo.android.viewmodel.MemoInputViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoInputPage(
    viewModel: MemoInputViewModel = hiltViewModel(),
    memoId: Long? = null,
    shareContent: ShareContent? = null
) {
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    val snackbarState = remember { SnackbarHostState() }
    val navController = LocalRootNavController.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val memosViewModel = LocalMemos.current
    val memo = remember { memosViewModel.memos.toList().find { it.id == memoId } }
    var text by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(memo?.content ?: "", TextRange(memo?.content?.length ?: 0)))
    }
    var visibilityMenuExpanded by remember { mutableStateOf(false) }
    var tagMenuExpanded by remember { mutableStateOf(false) }
    var photoImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val defaultVisibility =
        LocalUserState.current.currentUser?.memoVisibility ?: MemosVisibility.PRIVATE
    var currentVisibility by remember { mutableStateOf(memo?.visibility ?: defaultVisibility) }

    fun uploadImage(uri: Uri) = coroutineScope.launch {
        try {
            val bitmap =
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            viewModel.upload(bitmap).suspendOnSuccess {
                delay(300)
                focusRequester.requestFocus()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleTodoItem() {
        val contentBefore = text.text.substring(0, text.selection.min)
        val lastLineBreak = contentBefore.indexOfLast { it == '\n' }
        val nextLineBreak = text.text.indexOf('\n', lastLineBreak + 1)
        val currentLine = text.text.substring(
            lastLineBreak + 1, if (nextLineBreak == -1) text.text.length else nextLineBreak
        )
        val contentBeforeCurrentLine = contentBefore.substring(0, lastLineBreak + 1)
        val contentAfterCurrentLine =
            if (nextLineBreak == -1) "" else text.text.substring(nextLineBreak)

        for (prefix in LIST_ITEM_SYMBOL_LIST) {
            if (!currentLine.startsWith(prefix)) {
                continue
            }

            if (prefix == "- [ ] ") {
                text =
                    text.copy(contentBeforeCurrentLine + "- [x] " + currentLine.substring(prefix.length) + contentAfterCurrentLine)
                return
            }

            val offset = "- [ ] ".length - prefix.length
            text = text.copy(
                contentBeforeCurrentLine + "- [ ] " + currentLine.substring(prefix.length) + contentAfterCurrentLine,
                TextRange(text.selection.start + offset, text.selection.end + offset)
            )
            return
        }

        text = text.copy(
            "$contentBeforeCurrentLine- [ ] $currentLine$contentAfterCurrentLine",
            TextRange(text.selection.start + "- [ ] ".length, text.selection.end + "- [ ] ".length)
        )
    }

    fun handleEnter(): Boolean {
        val contentBefore = text.text.substring(0, text.selection.min)
        val lastLineBreak = contentBefore.indexOfLast { it == '\n' }
        val nextLineBreak = text.text.indexOf('\n', lastLineBreak + 1)
        val currentLine = text.text.substring(
            lastLineBreak + 1, if (nextLineBreak == -1) text.text.length else nextLineBreak
        )

        for (prefix in LIST_ITEM_SYMBOL_LIST) {
            if (!currentLine.startsWith(prefix)) {
                continue
            }

            if (currentLine.length <= prefix.length || text.selection.min - lastLineBreak <= prefix.length) {
                break
            }

            text = text.copy(
                text.text.substring(0, text.selection.min) + "\n" + prefix + text.text.substring(
                    text.selection.max
                ), TextRange(
                    text.selection.min + 1 + prefix.length, text.selection.min + 1 + prefix.length
                )
            )
            return true
        }

        return false
    }

    val pickImage = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        uri?.let { uploadImage(it) }
    }

    val takePhoto = rememberLauncherForActivityResult(TakePicture()) { success ->
        if (success) {
            photoImageUri?.let { uploadImage(it) }
        }
    }

    fun submit() = coroutineScope.launch {
        val tags = extractCustomTags(text.text)
        for (tag in tags) {
            viewModel.updateTag(tag)
                .suspendOnErrorMessage { message -> console.log("MemoInputPage submit $message") }
        }

        memo?.let {
            viewModel.editMemo(memo.id, text.text, currentVisibility).suspendOnSuccess {
                navController.popBackStack()
            }.suspendOnErrorMessage { message ->
                snackbarState.showSnackbar(message)
            }
            return@launch
        }

        viewModel.createMemo(text.text, currentVisibility).suspendOnSuccess {
            text = TextFieldValue("")
            viewModel.updateDraft("")
            navController.popBackStack()
        }.suspendOnErrorMessage { message ->
            snackbarState.showSnackbar(message)
        }
    }

    Scaffold(modifier = Modifier.imePadding(), topBar = {
        TopAppBar(
            title = {
                if (memo == null) {
                    Text(R.string.compose.string)
                } else {
                    Text(R.string.edit.string)
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    navController.popBackStackIfLifecycleIsResumed(lifecycleOwner)
                }) {
                    Icon(Icons.Filled.Close, contentDescription = R.string.close.string)
                }
            },
        )
    }, bottomBar = {
        BottomAppBar {
            Box {
                DropdownMenu(
                    expanded = visibilityMenuExpanded,
                    onDismissRequest = { visibilityMenuExpanded = false },
                    properties = PopupProperties(focusable = false)
                ) {
                    enumValues<MemosVisibility>().forEach { visibility ->
                        DropdownMenuItem(text = { Text(stringResource(visibility.titleResource)) },
                            onClick = {
                                currentVisibility = visibility
                                visibilityMenuExpanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    visibility.icon,
                                    contentDescription = stringResource(visibility.titleResource)
                                )
                            },
                            trailingIcon = {
                                if (currentVisibility == visibility) {
                                    Icon(Icons.Outlined.Check, contentDescription = null)
                                }
                            })
                    }
                }
                IconButton(onClick = { visibilityMenuExpanded = !visibilityMenuExpanded }) {
                    Icon(
                        currentVisibility.icon,
                        contentDescription = stringResource(currentVisibility.titleResource)
                    )
                }
            }

            if (memosViewModel.tags.toList().isEmpty()) {
                IconButton(onClick = {
                    text = text.copy(
                        text.text.replaceRange(text.selection.min, text.selection.max, "#"),
                        TextRange(text.selection.min + 1)
                    )
                }) {
                    Icon(Icons.Outlined.Tag, contentDescription = R.string.tag.string)
                }
            } else {
                Box {
                    DropdownMenu(
                        expanded = tagMenuExpanded,
                        onDismissRequest = { tagMenuExpanded = false },
                        properties = PopupProperties(focusable = false)
                    ) {
                        memosViewModel.tags.toList().forEach { tag ->
                            DropdownMenuItem(text = { Text(tag) }, onClick = {
                                val tagText = "#${tag} "
                                text = text.copy(
                                    text.text.replaceRange(
                                        text.selection.min, text.selection.max, tagText
                                    ), TextRange(text.selection.min + tagText.length)
                                )
                                tagMenuExpanded = false
                            }, leadingIcon = {
                                Icon(
                                    Icons.Outlined.Tag, contentDescription = null
                                )
                            })
                        }
                    }
                    IconButton(onClick = { tagMenuExpanded = !tagMenuExpanded }) {
                        Icon(Icons.Outlined.Tag, contentDescription = R.string.tag.string)
                    }
                }
            }

            IconButton(onClick = {
                toggleTodoItem()
            }) {
                Icon(Icons.Outlined.CheckBox, contentDescription = R.string.add_task.string)
            }

            IconButton(onClick = {
                pickImage.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            }) {
                Icon(Icons.Outlined.Image, contentDescription = R.string.add_image.string)
            }

            IconButton(onClick = {
                try {
                    val uri = MoeMemosFileProvider.getImageUri(context)
                    photoImageUri = uri
                    takePhoto.launch(uri)
                } catch (e: ActivityNotFoundException) {
                    coroutineScope.launch {
                        snackbarState.showSnackbar(
                            e.localizedMessage ?: "Unable to take picture."
                        )
                    }
                }
            }) {
                Icon(
                    Icons.Outlined.PhotoCamera, contentDescription = R.string.take_photo.string
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(enabled = text.text.isNotEmpty(), onClick = { submit() }) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = R.string.post.string)
            }
        }
    }, snackbarHost = {
        SnackbarHost(hostState = snackbarState)
    }) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxHeight()
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 30.dp)
                    .weight(1f)
                    .focusRequester(focusRequester),
//                    .onKeyEvent { event ->
//                        if (event.key == Key.Enter) {
//                            return@onKeyEvent handleEnter()
//                        }
//                        false
//                    },
                value = text,
                label = { Text(R.string.any_thoughts.string) },
                onValueChange = {
                    // an ugly hack to handle enter event, as `onKeyEvent` modifier only handles hardware keyboard,
                    // please submit a pull request if there's a native way to handle software key event
                    if (text.text != it.text && it.selection.start == it.selection.end && it.text.length == text.text.length + 1 && it.selection.start > 0 && it.text[it.selection.start - 1] == '\n') {
                        if (handleEnter()) {
                            return@OutlinedTextField
                        }
                    }
                    text = it
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            if (viewModel.uploadResources.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .height(80.dp)
                        .padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(viewModel.uploadResources.toList(), { it.id }) { resource ->
                        if (resource.type.startsWith("image/")) {
                            InputImage(resource = resource, inputViewModel = viewModel)
                        } else {
                            Attachment(resource = resource)
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        when {
            memo != null -> {
                memo.resourceList?.let { resourceList ->
                    viewModel.uploadResources.addAll(
                        resourceList
                    )
                }
            }

            shareContent != null -> {
                text = TextFieldValue(shareContent.text, TextRange(shareContent.text.length))
                for (item in shareContent.images) {
                    uploadImage(item)
                }
            }

            else -> {
                viewModel.draft.first()?.let {
                    text = TextFieldValue(it, TextRange(it.length))
                }
            }
        }
        delay(300)
        focusRequester.requestFocus()
    }

    DisposableEffect(Unit) {
        onDispose {
            if (memo == null && shareContent == null) {
                viewModel.updateDraft(text.text)
            }
        }
    }
}