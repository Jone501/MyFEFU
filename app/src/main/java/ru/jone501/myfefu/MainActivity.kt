package ru.jone501.myfefu

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.compose.koinInject
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.error.ApplicationAlreadyStartedException
import ru.jone501.myfefu.data.repository.EncryptedSessionManager
import ru.jone501.myfefu.di.coreModule
import ru.jone501.myfefu.lucide.CalendarDays
import ru.jone501.myfefu.lucide.MapPin
import ru.jone501.myfefu.lucide.QrCode
import ru.jone501.myfefu.networking.di.networkModule
import ru.jone501.myfefu.pages.LoginPage
import ru.jone501.myfefu.pages.SchedulePage
import ru.jone501.myfefu.ui.theme.Default
import ru.jone501.myfefu.ui.theme.MyFEFUTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            startKoin {
                androidLogger()
                androidContext(this@MainActivity)
                modules(
                    coreModule,
                    networkModule
                )
            }
        } catch (_: ApplicationAlreadyStartedException) {
        }
        enableEdgeToEdge()
        setContent {
            LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            val navController = rememberNavController()
            val sessionManager: EncryptedSessionManager = koinInject()

            LaunchedEffect("authorizationCheck") {
                if (sessionManager.get().first() == null) {
                    navController.navigate(Routes.Login)
                }
            }

            MyFEFUTheme(Default) {
                Scaffold { padding ->
                    NavHost(navController, Routes.Main) {
                        composable<Routes.Main> {
                            MainPageContainer(padding, navController)
                        }
                        composable<Routes.Login> {
                            LoginPage(padding, navController)
                        }
                    }
                }
            }
        }
    }
}

object Routes {
    @Serializable
    object Login

    @Serializable
    object Main
}

@Composable
fun MainPageContainer(padding: PaddingValues, navController: NavController, sessionManager: EncryptedSessionManager = koinInject()) {
    val paddingWithoutBottom = PaddingValues(
        padding.calculateLeftPadding(LayoutDirection.Ltr),
        padding.calculateTopPadding(),
        padding.calculateRightPadding(LayoutDirection.Ltr),
        0.dp
    )
    val selectedMenu = remember { mutableIntStateOf(2) }

    val coroutineScope = rememberCoroutineScope()

    Box(Modifier.padding(paddingWithoutBottom)) {
        SchedulePage(navController)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    0.dp,
                    0.dp,
                    0.dp,
                    padding.calculateBottomPadding()
                )
                .background(
                    MaterialTheme.colorScheme.background,
                    CircleShape
                )
                .padding(10.dp)
        ) {
            MenuButton(1, selectedMenu, onClick = {
                coroutineScope.launch {
                    sessionManager.clear()
                    navController.navigate(Routes.Login)
                }
            }) { x -> MapPin(x) }
            MenuButton(2, selectedMenu) { x -> CalendarDays(x) }
            MenuButton(3, selectedMenu) { x -> QrCode(x) }
        }
    }
}

@Composable
fun MenuButton(index: Int, selectedMenu: MutableState<Int>, onClick: () -> Unit = {}, iconFunction: (Color) -> ImageVector) {
    val selected = index == selectedMenu.value
    val backgroundColor = animateColorAsState(
        if (selected) MaterialTheme.colorScheme.surfaceTint
        else MaterialTheme.colorScheme.surface
    )
    val iconColor = animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onBackground
        else MaterialTheme.colorScheme.onSurfaceVariant
    )
    val outlineColor = animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onBackground
        else MaterialTheme.colorScheme.onBackground.copy(0f)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clickable(
                onClick = {
                    selectedMenu.value = index
                    onClick()
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .background(
                backgroundColor.value,
                CircleShape
            )
            .border(
                BorderStroke(2.dp, outlineColor.value),
                CircleShape
            )
            .size(50.dp)
            .aspectRatio(1f)
    ) {
        Image(
            iconFunction(iconColor.value),
            "image"
        )
    }
}

@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            activity.requestedOrientation = originalOrientation
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
@Preview
fun Preview() {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    val selectedMenu = remember { mutableIntStateOf(2) }

    MyFEFUTheme(Default, true) {
        Scaffold { padding ->
            val paddingWithoutBottom = PaddingValues(
                padding.calculateLeftPadding(LayoutDirection.Ltr),
                padding.calculateTopPadding(),
                padding.calculateRightPadding(LayoutDirection.Ltr),
                0.dp
            )
            Box(Modifier.padding(paddingWithoutBottom)) {
                SchedulePage(rememberNavController())
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(
                            0.dp,
                            0.dp,
                            0.dp,
                            padding.calculateBottomPadding()
                        )
                        .background(
                            MaterialTheme.colorScheme.background,
                            CircleShape
                        )
                        .padding(10.dp)
                ) {
                    MenuButton(1, selectedMenu) { x -> MapPin(x) }
                    MenuButton(2, selectedMenu) { x -> CalendarDays(x) }
                    MenuButton(3, selectedMenu) { x -> QrCode(x) }
                }
            }
        }
    }
}