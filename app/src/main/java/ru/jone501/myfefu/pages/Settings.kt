package ru.jone501.myfefu.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import ru.jone501.myfefu.R
import ru.jone501.myfefu.data.viewmodel.ProfileInfoViewModel
import ru.jone501.myfefu.navigateToLogin
import ru.jone501.myfefu.ui.lucide.LogOut
import ru.jone501.myfefu.ui.theme.MontserratAlternates

@Composable
fun SettingsPage(
    bottomPadding: Dp,
    navController: NavController,
    profileInfoViewModel: ProfileInfoViewModel = koinViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val profileInfo by profileInfoViewModel.profileInfo.collectAsState()
    val studentProfileInfo by profileInfoViewModel.studentProfileInfo.collectAsState()

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            modifier = Modifier
                .padding(0.dp, 25.dp, 0.dp, 90.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(25.dp, 0.dp)
            ) {
                Text(
                    stringResource(R.string.settings).uppercase(),
                    fontSize = 24.sp,
                    fontFamily = MontserratAlternates,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Column(
                modifier = Modifier
                    .padding(25.dp, 25.dp)
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AnimatedVisibility(profileInfo != null) {
                    Column {
                        Text(
                            "${profileInfo?.fullName}",
                            fontSize = 18.sp,
                            fontFamily = MontserratAlternates,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "${profileInfo?.username}",
                            fontSize = 16.sp,
                            fontFamily = MontserratAlternates,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                AnimatedVisibility(studentProfileInfo?.isEmpty()?.not() ?: false) {
                    Column {
                        Spacer(Modifier.height(25.dp))
                        Text(
                            "Группы:",
                            fontSize = 16.sp,
                            fontFamily = MontserratAlternates,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        studentProfileInfo?.forEach {
                            Text(
                                it.academicGroup,
                                fontSize = 18.sp,
                                fontFamily = MontserratAlternates,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(25.dp, 0.dp)
                    .fillMaxWidth()
            ) {
                SettingsButton(onClick = {
                    coroutineScope.launch {
                        profileInfoViewModel.clear()
                        navController.navigateToLogin()
                    }
                }) {
                    Text(
                        "Выйти из аккаунта".uppercase(),
                        fontSize = 14.sp,
                        fontFamily = MontserratAlternates,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.width(10.dp))
                    Icon(
                        LogOut(MaterialTheme.colorScheme.onBackground, 3f),
                        "Logout",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .padding(25.dp, bottomPadding + 10.dp)
                .align(Alignment.BottomEnd)
        ) {
            Text(
                "v${stringResource(R.string.current_app_version)}",
                fontSize = 16.sp,
                fontFamily = MontserratAlternates,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}

@Composable
fun SettingsButton(onClick: () -> Unit = {}, content: @Composable (RowScope.() -> Unit)) {
    Button(onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        content = content)
}