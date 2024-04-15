package cufoon.memo.android.ui.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


enum class ScreenSize {
    Phone, Pad
}

class ResponsiveInfo(width: Dp = 0.dp) {
    private var screen: ScreenSize

    init {
        screen = if (width < 675.dp) {
            ScreenSize.Phone
        } else {
            ScreenSize.Pad
        }
    }

    fun isPhone() = screen == ScreenSize.Phone

    fun isPad() = screen == ScreenSize.Pad
}

@Stable
interface ResponsiveInfoAndBoxConstraintCombinedScope {
    val responsiveInfo: ResponsiveInfo
    val boxWithConstraintsScope: BoxWithConstraintsScope
}

class ResponsiveInfoAndBoxConstraintCombinedScopeImpl(
    ri: ResponsiveInfo, bwcs: BoxWithConstraintsScope
) : ResponsiveInfoAndBoxConstraintCombinedScope {
    override val responsiveInfo: ResponsiveInfo = ri
    override val boxWithConstraintsScope: BoxWithConstraintsScope = bwcs
}

@Composable
fun CufoonMultiScreen(
    innerPaddingProvider: () -> PaddingValues = { PaddingValues() },
    content: @Composable @UiComposable ResponsiveInfoAndBoxConstraintCombinedScope.() -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    val innerPadding by rememberUpdatedState(innerPaddingProvider())
    val leftPadding = innerPadding.calculateRightPadding(layoutDirection)
    val rightPadding = innerPadding.calculateRightPadding(layoutDirection)
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val responsiveInfo = ResponsiveInfo(maxWidth - leftPadding - rightPadding)
        val contextScope = ResponsiveInfoAndBoxConstraintCombinedScopeImpl(responsiveInfo, this)
        contextScope.content()
    }
}