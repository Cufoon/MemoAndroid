package cufoon.memo.android.hook

import android.content.res.Configuration
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.parcelize.Parcelize


@Parcelize
data class Orientation(var orientation: Int, var isLandscape: Boolean) : Parcelable

@Composable
fun rememberOrientation(): Orientation {
    var result by rememberSaveable {
        mutableStateOf(
            Orientation(
                Configuration.ORIENTATION_PORTRAIT, false
            )
        )
    }

    val configuration = LocalConfiguration.current

    LaunchedEffect(configuration) { // Save any changes to the orientation value on the configuration object
        snapshotFlow { configuration.orientation }.collect {
            result = Orientation(it, it == Configuration.ORIENTATION_LANDSCAPE)
        }
    }

    return result
}