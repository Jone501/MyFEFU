package ru.jone501.myfefu.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import ru.jone501.myfefu.APP_VERSION
import ru.jone501.myfefu.R
import ru.jone501.myfefu.data.viewmodel.AppPreferences
import ru.jone501.myfefu.data.viewmodel.ProfileInfoViewModel
import ru.jone501.myfefu.navigateToLogin
import ru.jone501.myfefu.ui.lucide.ExternLink
import ru.jone501.myfefu.ui.theme.MontserratAlternates

@Composable
fun SettingsPage(
    bottomPadding: Dp,
    navController: NavController,
    onBack: () -> Unit = { }
) {
    BackHandler(onBack = onBack)
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(top = 25.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp)
            ) {
                Text(
                    stringResource(R.string.settings).uppercase(),
                    fontSize = 24.sp,
                    fontFamily = MontserratAlternates,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "${stringResource(R.string.app_name)} v$APP_VERSION",
                    fontSize = 16.sp,
                    fontFamily = MontserratAlternates,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            LazyColumn(
                modifier = Modifier
                    .padding(25.dp, 25.dp, 25.dp)
                    .clip(RoundedCornerShape(25.dp, 25.dp, 0.dp, 0.dp))
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content(navController)
                footer(bottomPadding)
            }
        }
        Box(
            Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background.copy(0f),
                        ),
                        start = Offset(0f, 250f),
                        end = Offset(0f, 0f)
                    )
                )
                .height(90.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        )
    }
}

private fun LazyListScope.content(
    navController: NavController
) {
    item {
        val preferences: AppPreferences = koinInject()
        val isWindowsEnabled by preferences.windowsEnabled.collectAsState()

        SettingsPane {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                ) {
                    Text(
                        stringResource(R.string.settings_show_windows_title),
                        fontSize = 20.sp,
                        fontFamily = MontserratAlternates,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        stringResource(R.string.settings_show_windows_desc),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontFamily = MontserratAlternates,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Switch(
                    checked = isWindowsEnabled,
                    onCheckedChange = { preferences.setWindowsEnabled(it) },
                    colors = SwitchDefaults.colors(
                        uncheckedBorderColor = MaterialTheme.colorScheme.onTertiary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onTertiary,
                        checkedBorderColor = MaterialTheme.colorScheme.onBackground,
                        checkedTrackColor = MaterialTheme.colorScheme.onBackground,
                        checkedThumbColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    }
    item {
        val uriHandler = LocalUriHandler.current

        SettingsPane(
            onClick = {
                uriHandler.openUri("https://t.me/my_fefu")
            }
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                ) {
                    Text(
                        stringResource(R.string.app_name),
                        fontSize = 20.sp,
                        fontFamily = MontserratAlternates,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        stringResource(R.string.settings_open_telegram_channel),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontFamily = MontserratAlternates,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Image(
                    imageVector = ExternLink(MaterialTheme.colorScheme.onBackground),
                    "Telegram channel",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
    item {
        val profileInfoViewModel: ProfileInfoViewModel = koinViewModel()
        val coroutineScope = rememberCoroutineScope()

        SettingsPane(
            onClick = {
                coroutineScope.launch {
                    profileInfoViewModel.clear()
                    navController.navigateToLogin()
                }
            },
            background = MaterialTheme.colorScheme.errorContainer
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.log_out),
                    fontSize = 20.sp,
                    fontFamily = MontserratAlternates,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

private fun LazyListScope.footer(
    bottomPadding: Dp
) {
    item {
        Spacer(Modifier.height(bottomPadding + 65.dp))
    }
}

@Composable
fun SettingsPane(
    onClick: (() -> Unit)? = null,
    background: Color = MaterialTheme.colorScheme.surface,
    content: @Composable (BoxScope.() -> Unit)
) {
    var modifier = Modifier.clip(RoundedCornerShape(25.dp))
    if (onClick != null)
        modifier = modifier.clickable(onClick = onClick)
    modifier = modifier
        .background(
            background,
            RoundedCornerShape(25.dp)
        )
        .padding(20.dp, 15.dp)
        .fillMaxWidth()
    Box(
        modifier = modifier,
        content = content
    )
}