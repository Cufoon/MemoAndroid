package cufoon.memo.android.hook

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import coil.ImageLoader

val LocalCoilImageLoader =
    compositionLocalOf<ImageLoader> { error("LocalCoilImageLoader is null") }

@Composable
fun useCoilImageLoader(): ImageLoader {
    return LocalCoilImageLoader.current
}
