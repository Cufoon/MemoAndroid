package cufoon.memo.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import cufoon.memo.android.hook.LocalCoilImageLoader
import cufoon.memo.android.ui.page.common.Navigation
import cufoon.memo.android.util.initConsoleLog
import cufoon.memo.android.viewmodel.LocalMemos
import cufoon.memo.android.viewmodel.LocalUserState
import cufoon.memo.android.viewmodel.MemosViewModel
import cufoon.memo.android.viewmodel.UserStateViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val userStateViewModel: UserStateViewModel by viewModels()
    private val memosViewModel: MemosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        initConsoleLog()
        val imageLoader =
            ImageLoader.Builder(this)
                .okHttpClient(userStateViewModel.okHttpClient)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .memoryCache {
                    MemoryCache.Builder(this)
                        .maxSizePercent(0.25)
                        .build()
                }
                .diskCachePolicy(CachePolicy.ENABLED)
                .diskCache {
                    DiskCache.Builder()
                        .directory(this.cacheDir.resolve("cufoon_coil_image_cache"))
                        .maxSizePercent(0.02)
                        .build()
                }
                .logger(DebugLogger())
                .build()
        setContent {
            CompositionLocalProvider(
                LocalUserState provides userStateViewModel,
                LocalMemos provides memosViewModel,
                LocalCoilImageLoader provides imageLoader
            ) {
                Navigation()
            }
        }
    }
}