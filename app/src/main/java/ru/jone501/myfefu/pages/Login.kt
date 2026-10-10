package ru.jone501.myfefu.pages

import android.view.KeyEvent.ACTION_DOWN
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import ru.jone501.myfefu.R
import ru.jone501.myfefu.Routes
import ru.jone501.myfefu.data.viewmodel.LessonsViewModel
import ru.jone501.myfefu.data.viewmodel.ProfileInfoViewModel
import ru.jone501.myfefu.domain.model.AuthToken
import ru.jone501.myfefu.domain.repository.SessionManager
import ru.jone501.myfefu.networking.token.TokenService
import ru.jone501.myfefu.networking.token.request.CreateTokenRequest
import ru.jone501.myfefu.ui.lucide.Eye
import ru.jone501.myfefu.ui.lucide.EyeClosed
import ru.jone501.myfefu.ui.theme.MontserratAlternates

@Composable
fun LoginPage(
    padding: PaddingValues,
    navController: NavController,
    tokenService: TokenService = koinInject(),
    profileInfoViewModel: ProfileInfoViewModel = koinViewModel(),
    lessonsViewModel: LessonsViewModel = koinViewModel(),
    sessionManager: SessionManager = koinInject()
) {
    var login by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showInvalidCredentialsLabel by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(padding)
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(0.dp, 0.dp, 0.dp, 50.dp)
        ) {
            Icon(
                painterResource(R.drawable.fefu_logo),
                "fefu_logo",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.height(150.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.login).uppercase(),
                    fontSize = 24.sp,
                    fontFamily = MontserratAlternates,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                val textErrorColor = animateColorAsState(
                    if (showInvalidCredentialsLabel) MaterialTheme.colorScheme.onError
                    else MaterialTheme.colorScheme.onBackground
                )
                val focusManager = LocalFocusManager.current
                val textFieldColors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    errorContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    cursorColor = textErrorColor.value,
                )
                val textStyle = TextStyle(
                    color = textErrorColor.value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = MontserratAlternates
                )
                TextField(
                    modifier = Modifier
                        .width(300.dp)
                        .onPreviewKeyEvent {
                            if (it.key == Key.Tab && it.nativeKeyEvent.action == ACTION_DOWN) {
                                focusManager.moveFocus(FocusDirection.Down)
                                true
                            } else {
                                false
                            }
                        },
                    value = login,
                    onValueChange = {
                        login = it
                        showInvalidCredentialsLabel = false
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = showInvalidCredentialsLabel,
                    colors = textFieldColors,
                    textStyle = textStyle,
                    shape = CircleShape,
                    singleLine = true,
                    placeholder = {
                        Text(
                            stringResource(R.string.login_literally),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 18.sp,
                            fontFamily = MontserratAlternates
                        )
                    }
                )
                TextField(
                    modifier = Modifier
                        .width(300.dp),
                    value = password,
                    onValueChange = {
                        password = it
                        showInvalidCredentialsLabel = false
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    colors = textFieldColors,
                    textStyle = textStyle,
                    shape = CircleShape,
                    singleLine = true,
                    placeholder = {
                        Text(
                            stringResource(R.string.password),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 18.sp,
                            fontFamily = MontserratAlternates
                        )
                    },
                    trailingIcon = {
                        Icon(
                            if (showPassword) Eye(MaterialTheme.colorScheme.onBackground)
                            else EyeClosed(MaterialTheme.colorScheme.onBackground),
                            "togglePasswordButton",
                            modifier = Modifier
                                .clickable(
                                    onClick = {
                                        showPassword = !showPassword
                                    },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                )
                        )
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                )
                Button(
                    onClick = {
                        showInvalidCredentialsLabel = false
                        coroutineScope.launch {
                            val response = tokenService.createToken(
                                CreateTokenRequest(
                                    login,
                                    password,
                                )
                            )
                            val responseBody = response.body()
                            if (response.isSuccessful && responseBody != null) {
                                sessionManager.setToken(
                                    AuthToken(
                                        responseBody.access_token,
                                        responseBody.refresh_token
                                    )
                                )
                                profileInfoViewModel.init()
                                lessonsViewModel.init()
                                navController.navigate(Routes.MAIN)
                            } else {
                                showInvalidCredentialsLabel = true
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Text(
                        stringResource(R.string.login_in).uppercase(),
                        fontSize = 20.sp,
                        fontFamily = MontserratAlternates,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                val invalidCredentialsTextColor = animateColorAsState(
                    if (showInvalidCredentialsLabel) MaterialTheme.colorScheme.onError
                    else MaterialTheme.colorScheme.onError.copy(0f)
                )
                Text(
                    stringResource(R.string.invalid_credentials),
                    fontSize = 16.sp,
                    fontFamily = MontserratAlternates,
                    fontWeight = FontWeight.Normal,
                    color = invalidCredentialsTextColor.value
                )
            }
        }
    }
}