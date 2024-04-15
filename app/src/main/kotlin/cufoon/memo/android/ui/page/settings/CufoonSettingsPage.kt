package cufoon.memo.android.ui.page.settings

import android.icu.text.DateFormatSymbols
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Web
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cufoon.memo.android.R
import cufoon.memo.android.ext.string
import cufoon.memo.android.ui.component.CufoonMultiScreen
import cufoon.memo.android.ui.component.Heatmap
import cufoon.memo.android.ui.page.common.RouteName
import cufoon.memo.android.viewmodel.LocalMemos
import cufoon.memo.android.viewmodel.LocalUserState
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CufoonSettingsPage(navController: NavHostController, outerPaddingValues: PaddingValues) {
    val weekDays = remember { DateFormatSymbols.getInstance().shortWeekdays }
    val userStateViewModel = LocalUserState.current
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val status = userStateViewModel.status
    val memosViewModel = LocalMemos.current

    Scaffold(modifier = Modifier.padding(outerPaddingValues),
        topBar = { TopAppBar(title = { Text(text = R.string.settings.string) }) }) { innerPadding ->
        CufoonSettingPageLayout({ innerPadding }, slotTop = {
            userStateViewModel.currentUser?.let { user ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp)
                ) {
                    Column(Modifier.padding(15.dp)) {
                        Text(user.displayName, style = MaterialTheme.typography.headlineSmall)
                        if (user.displayName != user.displayEmail && user.displayEmail.isNotEmpty()) {
                            Text(
                                user.displayEmail,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        if (status?.profile?.version?.isNotEmpty() == true) {
                            Text(
                                "✍️memos v${status.profile.version}",
                                modifier = Modifier.padding(top = 5.dp),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } ?: Button(
                onClick = {
                    navController.navigate(RouteName.LOGIN)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Text(R.string.sign_in.string, style = MaterialTheme.typography.titleLarge)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(120.dp),
                Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 5.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        weekDays[1],
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        weekDays[4],
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        weekDays[7],
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Heatmap()
            }
        }) {
            Text(
                R.string.about.string,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 30.dp, 24.dp, 16.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
            Surface(onClick = {
                uriHandler.openUri("https://xyl.cool")
            }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.Web,
                        contentDescription = R.string.web.string,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        R.string.website.string,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (userStateViewModel.currentUser != null) {
                FilledTonalButton(
                    onClick = {
                        coroutineScope.launch {
                            userStateViewModel.logout()
                            navController.navigate(RouteName.LOGIN) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .padding(20.dp),
                    contentPadding = PaddingValues(10.dp)
                ) {
                    Text(R.string.sign_out.string)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        memosViewModel.loadTags()
    }
}

@Composable
fun CufoonSettingPageLayout(
    innerPaddingProvider: () -> PaddingValues,
    slotTop: @Composable () -> Unit,
    slotBottom: @Composable () -> Unit
) {
    val innerPadding by rememberUpdatedState(innerPaddingProvider())

    CufoonMultiScreen(innerPaddingProvider) {
        if (responsiveInfo.isPhone()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                slotTop()
                slotBottom()
            }
        } else {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    slotTop()
                }
                Column(
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    slotBottom()
                }
            }
        }
    }
}
