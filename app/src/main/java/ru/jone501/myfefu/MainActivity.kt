package ru.jone501.myfefu

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.GlobalContext.startKoin
import ru.jone501.myfefu.data.repository.EncryptedSessionManager
import ru.jone501.myfefu.data.viewmodel.LessonsViewModel
import ru.jone501.myfefu.data.viewmodel.ProfileInfoViewModel
import ru.jone501.myfefu.di.coreModule
import ru.jone501.myfefu.di.viewModelModule
import ru.jone501.myfefu.networking.di.networkModule
import ru.jone501.myfefu.pages.LoginPage
import ru.jone501.myfefu.pages.SchedulePage
import ru.jone501.myfefu.pages.SettingsPage
import ru.jone501.myfefu.ui.lucide.CalendarDays
import ru.jone501.myfefu.ui.lucide.Settings
import ru.jone501.myfefu.ui.theme.Default
import ru.jone501.myfefu.ui.theme.MyFEFUTheme

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(
                coreModule,
                networkModule,
                viewModelModule,
            )
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            val navController = rememberNavController()
            val sessionManager: EncryptedSessionManager = koinInject()

            LaunchedEffect("authorizationCheck") {
                if (sessionManager.getToken().firstOrNull() == null) {
                    navController.navigateToLogin()
                }
            }

            val profileInfoViewModel: ProfileInfoViewModel = koinViewModel()
            val lessonsViewModel: LessonsViewModel = koinViewModel()
            val requiresLoginByProfileViewModel: Boolean by profileInfoViewModel.requiresLogin.collectAsState()
            val requiresLoginByLessonsViewModel: Boolean by lessonsViewModel.requiresLogin.collectAsState()
            if (requiresLoginByProfileViewModel || requiresLoginByLessonsViewModel) {
                profileInfoViewModel.clear()
                navController.navigateToLogin()
            }

            MyFEFUTheme(Default) {
                Scaffold { padding ->
                    NavHost(navController, Routes.MAIN) {
                        composable(Routes.MAIN) {
                            MainPageContainer(padding, navController)
                        }
                        composable(Routes.LOGIN) {
                            BackHandler { }
                            LoginPage(padding, navController)
                        }
                    }
                }
            }
        }
    }
}

fun NavController.navigateToLogin() {
    if (this.currentBackStackEntry?.destination?.route != Routes.LOGIN)
        this.navigate(Routes.LOGIN)
}

object Routes {
    const val LOGIN = "LOGIN"
    const val MAIN = "MAIN"
}

@Composable
fun MainPageContainer(
    padding: PaddingValues,
    navController: NavController
) {
    val paddingWithoutBottom = PaddingValues(
        padding.calculateLeftPadding(LayoutDirection.Ltr),
        padding.calculateTopPadding(),
        padding.calculateRightPadding(LayoutDirection.Ltr),
        0.dp
    )
    val selectedMenu = rememberSaveable { mutableIntStateOf(1) }
    val coroutineScope = rememberCoroutineScope()

    val pagerState = rememberPagerState { 2 }

    Box(Modifier
        .padding(0.dp, 10.dp)
        .padding(paddingWithoutBottom)) {
        HorizontalPager(pagerState, userScrollEnabled = false) { page ->
            when (page) {
                0 -> SchedulePage()
                1 -> SettingsPage(
                    padding.calculateBottomPadding(),
                    navController
                )
            }
        }
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
                    pagerState.animateScrollToPage(0)
                }
            }) { x -> CalendarDays(x) }
            MenuButton(2, selectedMenu, onClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(1)
                }
            }) { x -> Settings(x) }
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