package ru.jone501.myfefu.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ru.jone501.myfefu.R
import ru.jone501.myfefu.Routes
import ru.jone501.myfefu.data.repository.EncryptedSessionManager
import ru.jone501.myfefu.domain.model.AuthToken
import ru.jone501.myfefu.networking.token.TokenService
import ru.jone501.myfefu.networking.token.request.CreateTokenRequest
import ru.jone501.myfefu.ui.theme.Default
import ru.jone501.myfefu.ui.theme.MontserratAlternates
import ru.jone501.myfefu.ui.theme.MyFEFUTheme

@Composable
fun LoginPage(padding: PaddingValues, navController: NavController, tokenService: TokenService = koinInject(), sessionManager: EncryptedSessionManager = koinInject()) {
    val loginFieldState = rememberTextFieldState()
    val passwordFieldState = rememberTextFieldState()
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
                LoginPageInputField(
                    loginFieldState,
                    placeholder = {
                        Text(
                            "Логин",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 18.sp,
                            fontFamily = MontserratAlternates
                        )
                    }
                )
                LoginPageInputField(
                    passwordFieldState,
                    placeholder = {
                        Text(
                            "Пароль",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 18.sp,
                            fontFamily = MontserratAlternates
                        )
                    },
                )
                Button(onClick = {
                    coroutineScope.launch {
                        val response = tokenService.createToken(
                            CreateTokenRequest(
                                loginFieldState.text.toString(),
                                passwordFieldState.text.toString(),
                            )
                        )
                        val responseBody = response.body()
                        if (response.isSuccessful && responseBody != null) {
                            Log.i("SESSION", "${loginFieldState.text} ${passwordFieldState.text}")
                            sessionManager.set(AuthToken(
                                responseBody.access_token,
                                responseBody.refresh_token
                            ))
                            navController.navigate(Routes.Main)
                        }
                    }
                },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceTint
                    )) {
                    Text(
                        stringResource(R.string.login_verb).uppercase(),
                        fontSize = 20.sp,
                        fontFamily = MontserratAlternates,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginPageInputField(
    textFieldState: TextFieldState,
    placeholder: @Composable (() -> Unit)? = null,
    colors: TextFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        cursorColor = MaterialTheme.colorScheme.onBackground,
    )
) {
    TextField(
        textFieldState,
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = MontserratAlternates
        ),
        placeholder = placeholder,
        shape = CircleShape,
        colors = colors
    )
}

@Composable
@Preview
fun PreviewLoginPage() {
    MyFEFUTheme(Default, true) {
        LoginPage(PaddingValues(0.dp), rememberNavController())
    }
}