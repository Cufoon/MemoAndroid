package cufoon.memo.android.ui.component

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImage
import coil.imageLoader
import cufoon.memo.android.hook.useCoilImageLoader
import cufoon.memo.android.util.console
import java.io.File


@OptIn(ExperimentalCoilApi::class)
@Composable
fun MemoImage(
    url: String,
    modifier: Modifier = Modifier
) {
    var diskCacheFile: File? by remember { mutableStateOf(null) }
    val context = LocalContext.current
    val imageLoader = useCoilImageLoader()

    AsyncImage(
        model = url,
        imageLoader = imageLoader,
        contentDescription = null,
        modifier = modifier.clickable {
            diskCacheFile?.let {
                val fileUri: Uri = try {
                    FileProvider.getUriForFile(context, context.packageName + ".fileprovider", it)
                } catch (e: Throwable) {
                    console.log(e)
                    null
                } ?: return@let

                val intent = Intent().apply {
                    action = Intent.ACTION_VIEW
                    setDataAndType(fileUri, "image/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                try {
                    context.startActivity(intent)
                } catch (e: Throwable) {
                    console.log(e)
                }
            }
        },
        contentScale = ContentScale.Crop,
        onSuccess = { state ->
            val diskCache = context.imageLoader.diskCache
            val diskCacheKey = state.result.diskCacheKey

            if (diskCache != null && diskCacheKey != null) {
                diskCacheFile = diskCache.openSnapshot(diskCacheKey)?.data?.toFile()
            }
        }
    )
}