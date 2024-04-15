package cufoon.memo.android.ui.page.login

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.PermIdentity
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavHostController
import com.skydoves.sandwich.suspendOnSuccess
import cufoon.memo.android.R
import cufoon.memo.android.ext.string
import cufoon.memo.android.ext.suspendOnErrorMessage
import cufoon.memo.android.ui.component.Markdown
import cufoon.memo.android.ui.page.common.RouteName
import cufoon.memo.android.viewmodel.LocalUserState
import kotlinx.coroutines.launch


private enum class LoginMethod {
    USERNAME_AND_PASSWORD, ACCESS_TOKEN
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoginPage(
    navController: NavHostController
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val userStateViewModel = LocalUserState.current
    val snackBarState = remember { SnackbarHostState() }
    val layoutDirection = LocalLayoutDirection.current

    var loginMethodMenuExpanded by remember { mutableStateOf(false) }
    var loginMethod by remember { mutableStateOf(LoginMethod.USERNAME_AND_PASSWORD) }

    var username by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(MEMOS_DEFAULT_USERNAME))
    }

    var password by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    val host by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(MEMOS_HOST_URL))
    }

    var accessToken by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    fun login() = coroutineScope.launch {
        if (host.text.isBlank() || (loginMethod == LoginMethod.USERNAME_AND_PASSWORD && (username.text.isBlank() || password.text.isEmpty())) || (loginMethod == LoginMethod.ACCESS_TOKEN && (accessToken.text.isBlank()))) {
            snackBarState.showSnackbar(R.string.fill_login_form.string)
            return@launch
        }

        val resp = when (loginMethod) {
            LoginMethod.USERNAME_AND_PASSWORD -> userStateViewModel.login(
                host.text.trim(), username.text.trim(), password.text
            )

            LoginMethod.ACCESS_TOKEN -> userStateViewModel.loginWithAccessToken(
                host.text.trim(), accessToken.text.trim()
            )
        }

        resp.suspendOnSuccess {
            navController.popBackStack()
            navController.navigate(RouteName.MEMOS) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
        }.suspendOnErrorMessage {
            snackBarState.showSnackbar(it)
        }
    }

    Scaffold(modifier = Modifier.imePadding(), snackbarHost = {
        SnackbarHost(hostState = snackBarState)
    }, bottomBar = {
        BottomAppBar(actions = {
            Box {
                DropdownMenu(
                    expanded = loginMethodMenuExpanded,
                    onDismissRequest = { loginMethodMenuExpanded = false },
                    properties = PopupProperties(focusable = false)
                ) {
                    DropdownMenuItem(text = { Text(R.string.username_and_password.string) },
                        onClick = {
                            loginMethod = LoginMethod.USERNAME_AND_PASSWORD
                            loginMethodMenuExpanded = false
                        },
                        trailingIcon = {
                            if (loginMethod == LoginMethod.USERNAME_AND_PASSWORD) {
                                Icon(
                                    Icons.Outlined.Check,
                                    contentDescription = R.string.icon_description_check.string
                                )
                            }
                        })
                    DropdownMenuItem(text = { Text(R.string.access_token.string) }, onClick = {
                        loginMethod = LoginMethod.ACCESS_TOKEN
                        loginMethodMenuExpanded = false
                    }, trailingIcon = {
                        if (loginMethod == LoginMethod.ACCESS_TOKEN) {
                            Icon(
                                Icons.Outlined.Check,
                                contentDescription = R.string.icon_description_check.string
                            )
                        }
                    })
                }
                TextButton(onClick = { loginMethodMenuExpanded = true }) {
                    Text(R.string.sign_in_method.string)
                }
            }
        }, floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { login() },
                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                text = { Text(R.string.sign_in.string) },
                icon = {
                    Icon(
                        Icons.AutoMirrored.Outlined.Login,
                        contentDescription = R.string.sign_in.string
                    )
                })
        })
    }) { innerPadding ->
        val leftPadding = innerPadding.calculateRightPadding(layoutDirection)
        val rightPadding = innerPadding.calculateRightPadding(layoutDirection)
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val isPageWide = (maxWidth - leftPadding - rightPadding) > 600.dp
            Column(
                Modifier
                    .padding(innerPadding)
                    .fillMaxHeight()
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LoginPageDynamicLayout(isPageWideProvider = { isPageWide }) {
                    Column(
                        Modifier
                            .padding(horizontal = 30.dp)
                            .widthIn(max = 300.dp)
                            .bringIntoViewRequester(bringIntoViewRequester),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LoginInputArea(bringIntoViewRequesterProvider = { bringIntoViewRequester },
                            loginMethodProvider = { loginMethod },
                            userNameProvider = { username },
                            onUserNameChange = { username = it },
                            passWordProvider = { password },
                            onPassWordChange = { password = it },
                            accessTokenProvider = { accessToken },
                            onAccessTokenChange = { accessToken = it },
                            onClickEnter = { login() })
                    }
                }
            }
        }
    }
}

@Composable
fun LoginPageDynamicLayout(isPageWideProvider: () -> Boolean, content: @Composable () -> Unit) {
    val isPageWide by rememberUpdatedState(isPageWideProvider())
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                R.string.moe_memos.string,
                modifier = Modifier.padding(bottom = 10.dp),
                style = MaterialTheme.typography.titleLarge
            )
            Markdown(
                R.string.input_login_information.string,
                modifier = Modifier.padding(bottom = 20.dp),
                textAlign = TextAlign.Center
            )
            if (!isPageWide) {
                content()
            }
        }
        if (isPageWide) {
            Column(Modifier.weight(1f)) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LoginInputArea(
    bringIntoViewRequesterProvider: () -> BringIntoViewRequester,
    loginMethodProvider: () -> LoginMethod,
    userNameProvider: () -> TextFieldValue,
    onUserNameChange: (TextFieldValue) -> Unit,
    passWordProvider: () -> TextFieldValue,
    onPassWordChange: (TextFieldValue) -> Unit,
    accessTokenProvider: () -> TextFieldValue,
    onAccessTokenChange: (TextFieldValue) -> Unit,
    onClickEnter: KeyboardActionScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val bringIntoViewRequester by rememberUpdatedState(bringIntoViewRequesterProvider())
    val loginMethod by rememberUpdatedState(loginMethodProvider())
    val username by rememberUpdatedState(userNameProvider())
    val password by rememberUpdatedState(passWordProvider())
    val accessToken by rememberUpdatedState(accessTokenProvider())

    //OutlinedTextField(
    //    modifier = Modifier
    //        .fillMaxWidth()
    //        .onFocusEvent { focusState ->
    //            if (focusState.isFocused) {
    //                coroutineScope.launch {
    //                    bringIntoViewRequester.bringIntoView()
    //                }
    //            }
    //        },
    //    value = host,
    //    onValueChange = { host = it },
    //    singleLine = true,
    //    leadingIcon = {
    //        Icon(
    //            imageVector = Icons.Outlined.Computer,
    //            contentDescription = R.string.address.string
    //        )
    //    },
    //    label = {
    //        Text(R.string.host.string)
    //    },
    //    keyboardOptions = KeyboardOptions(
    //        capitalization = KeyboardCapitalization.None,
    //        autoCorrect = false,
    //        keyboardType = KeyboardType.Uri,
    //        imeAction = ImeAction.Next
    //    )
    //)

    if (loginMethod == LoginMethod.USERNAME_AND_PASSWORD) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusEvent { focusState ->
                    if (focusState.isFocused) {
                        coroutineScope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                },
            value = username,
            onValueChange = onUserNameChange,
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = R.string.username.string
                )
            },
            label = { Text(R.string.username.string) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrect = false,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusEvent { focusState ->
                    if (focusState.isFocused) {
                        coroutineScope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                },
            value = password,
            onValueChange = onPassWordChange,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Password,
                    contentDescription = R.string.password.string
                )
            },
            label = { Text(R.string.password.string) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrect = false,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Go
            ),
            keyboardActions = KeyboardActions(onGo = onClickEnter)
        )
    }
    if (loginMethod == LoginMethod.ACCESS_TOKEN) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusEvent { focusState ->
                    if (focusState.isFocused) {
                        coroutineScope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                },
            value = accessToken,
            onValueChange = onAccessTokenChange,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.PermIdentity,
                    contentDescription = R.string.access_token.string
                )
            },
            label = {
                Text(R.string.access_token.string)
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrect = false,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Go
            ),
            keyboardActions = KeyboardActions(onGo = onClickEnter)
        )
    }
}
