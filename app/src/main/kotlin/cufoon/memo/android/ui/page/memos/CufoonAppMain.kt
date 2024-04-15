package cufoon.memo.android.ui.page.memos

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cufoon.memo.android.R
import cufoon.memo.android.ext.string
import cufoon.memo.android.ext.suspendOnNotLogin
import cufoon.memo.android.ui.page.common.RouteName
import cufoon.memo.android.ui.page.resource.CufoonResourceListPage
import cufoon.memo.android.ui.page.settings.CufoonSettingsPage
import cufoon.memo.android.util.console
import cufoon.memo.android.viewmodel.LocalUserState


@Composable
fun CufoonAppMain(rootNC: NavHostController) {
    val appMainNC = rememberNavController()
    val userStateViewModel = LocalUserState.current

    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = { Row(Modifier.height(0.dp)) {} },
        bottomBar = { BottomBar(navController = appMainNC) }) { innerPadding ->
        NavHost(modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            navController = appMainNC,
            startDestination = appMainNC.currentDestination?.route ?: RouteName.MEMOS,
            enterTransition = {
                fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                    initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(90))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(
                    initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(90))
            }) {
            composable(RouteName.MEMOS) {
                CufoonMemosHomePage(rootNC, innerPadding)
            }
            composable(RouteName.TAG) {
                CufoonTagPage(rootNC, innerPadding)
            }
            composable(RouteName.RESOURCE) {
                CufoonResourceListPage(innerPadding)
            }
            composable(RouteName.ARCHIVED) {
                ArchivedMemoList(outerPadding = innerPadding)
            }
            composable(RouteName.SETTINGS) {
                CufoonSettingsPage(rootNC, innerPadding)
            }
        }
    }

    LaunchedEffect(Unit) {
        console.log("CufoonAppMain userStateViewModel.loadCurrentUser()")
        userStateViewModel.loadCurrentUser().suspendOnNotLogin {
            if (rootNC.currentDestination?.route != RouteName.LOGIN) {
                rootNC.navigate(RouteName.LOGIN) {
                    popUpTo(rootNC.graph.startDestinationId) {
                        inclusive = true
                    }
                }
            }
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val selectedIndex = rememberSaveable {
        mutableStateOf(
            navController.currentDestination?.route ?: RouteName.MEMOS
        )
    }

    NavigationBar(tonalElevation = 10.dp, windowInsets = WindowInsets.navigationBars) {
        NavigationBarItem(icon = {
            Icon(Icons.Outlined.GridView, "")
        },
            label = { Text(R.string.memos.string) },
            selected = (selectedIndex.value == RouteName.MEMOS),
            onClick = {
                if (selectedIndex.value != RouteName.MEMOS) {
                    val preSelectedIndex = selectedIndex.value
                    selectedIndex.value = RouteName.MEMOS
                    navController.navigate(RouteName.MEMOS) {
                        popUpTo(preSelectedIndex) {
                            inclusive = true
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            })

        NavigationBarItem(icon = {
            Icon(Icons.Outlined.Tag, "")
        },
            label = { Text(R.string.tags.string) },
            selected = (selectedIndex.value == RouteName.TAG),
            onClick = {
                if (selectedIndex.value != RouteName.TAG) {
                    val preSelectedIndex = selectedIndex.value
                    selectedIndex.value = RouteName.TAG
                    navController.navigate(RouteName.TAG) {
                        popUpTo(preSelectedIndex) {
                            inclusive = true
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            })

        NavigationBarItem(icon = {
            Icon(imageVector = Icons.Outlined.PhotoLibrary, "")
        },
            label = { Text(R.string.resources.string) },
            selected = (selectedIndex.value == RouteName.RESOURCE),
            onClick = {
                if (selectedIndex.value != RouteName.RESOURCE) {
                    val preSelectedIndex = selectedIndex.value
                    selectedIndex.value = RouteName.RESOURCE
                    navController.navigate(RouteName.RESOURCE) {
                        popUpTo(preSelectedIndex) {
                            inclusive = true
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            })

        NavigationBarItem(icon = {
            Icon(imageVector = Icons.Outlined.Inventory2, "")
        },
            label = { Text(R.string.archived.string) },
            selected = (selectedIndex.value == RouteName.ARCHIVED),
            onClick = {
                if (selectedIndex.value != RouteName.ARCHIVED) {
                    val preSelectedIndex = selectedIndex.value
                    selectedIndex.value = RouteName.ARCHIVED
                    navController.navigate(RouteName.ARCHIVED) {
                        popUpTo(preSelectedIndex) {
                            inclusive = true
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            })

        NavigationBarItem(icon = {
            Icon(imageVector = Icons.Outlined.Settings, "")
        },
            label = { Text(R.string.settings.string) },
            selected = (selectedIndex.value == RouteName.SETTINGS),
            onClick = {
                if (selectedIndex.value != RouteName.SETTINGS) {
                    val preSelectedIndex = selectedIndex.value
                    selectedIndex.value = RouteName.SETTINGS
                    navController.navigate(RouteName.SETTINGS) {
                        popUpTo(preSelectedIndex) {
                            inclusive = true
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            })
    }
}
