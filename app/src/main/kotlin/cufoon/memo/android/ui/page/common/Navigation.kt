package cufoon.memo.android.ui.page.common

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cufoon.memo.android.data.model.ShareContent
import cufoon.memo.android.ext.string
import cufoon.memo.android.ext.suspendOnNotLogin
import cufoon.memo.android.ui.page.login.LoginPage
import cufoon.memo.android.ui.page.memoinput.MemoInputPage
import cufoon.memo.android.ui.page.memos.CufoonAppMain
import cufoon.memo.android.ui.page.memos.CufoonTagMemoPage
import cufoon.memo.android.ui.page.memos.SearchPage
import cufoon.memo.android.ui.theme.MoeMemosTheme
import cufoon.memo.android.util.console
import cufoon.memo.android.viewmodel.LocalUserState


@Composable
fun Navigation() {
    val navController = rememberNavController()
    val userStateViewModel = LocalUserState.current
    val context = LocalContext.current
    var shareContent by remember { mutableStateOf<ShareContent?>(null) }

    CompositionLocalProvider(LocalRootNavController provides navController) {
        MoeMemosTheme {
            NavHost(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                navController = navController,
                startDestination = RouteName.MEMOS,
                enterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(220, delayMillis = 90)
                    )
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(90))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(220, delayMillis = 90)
                    )
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(90))
                }
            ) {
                composable(RouteName.MEMOS) {
                    CufoonAppMain(navController)
                }
                composable(RouteName.LOGIN) {
                    LoginPage(navController = navController)
                }

                composable(RouteName.INPUT) {
                    MemoInputPage()
                }

                composable(RouteName.SHARE) {
                    MemoInputPage(shareContent = shareContent)
                }

                composable(
                    "${RouteName.EDIT}?memoId={id}"
                ) { entry ->
                    MemoInputPage(memoId = entry.arguments?.getString("id")?.toLong())
                }

                composable(
                    "${RouteName.TAG}/{tag}"
                ) { entry ->
                    CufoonTagMemoPage(navController, entry.arguments?.getString("tag") ?: "")
                }

                composable(RouteName.SEARCH) {
                    SearchPage()
                }
            }
        }
    }


    LaunchedEffect(Unit) {
        console.log("userStateViewModel.loadCurrentUser()")
        userStateViewModel.loadCurrentUser().suspendOnNotLogin {
            if (navController.currentDestination?.route != RouteName.LOGIN) {
                navController.navigate(RouteName.LOGIN) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
            }
        }
    }

    fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE -> {
                shareContent = ShareContent.parseIntent(intent)
                navController.navigate(RouteName.SHARE)
            }
        }
    }

    LaunchedEffect(context) {
        if (context is ComponentActivity && context.intent != null) {
            handleIntent(context.intent)
        }
    }

    DisposableEffect(context) {
        val activity = context as? ComponentActivity

        val listener = Consumer<Intent> {
            handleIntent(it)
        }

        activity?.addOnNewIntentListener(listener)

        onDispose {
            activity?.removeOnNewIntentListener(listener)
        }
    }
}

val LocalRootNavController =
    compositionLocalOf<NavHostController> { error(cufoon.memo.android.R.string.nav_host_controller_not_found.string) }