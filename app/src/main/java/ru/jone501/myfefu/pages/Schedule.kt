package ru.jone501.myfefu.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import ru.jone501.myfefu.R
import ru.jone501.myfefu.data.viewmodel.AppPreferences
import ru.jone501.myfefu.data.viewmodel.LessonsViewModel
import ru.jone501.myfefu.domain.model.EmptyLesson
import ru.jone501.myfefu.domain.model.Lesson
import ru.jone501.myfefu.domain.model.facilityPrettier
import ru.jone501.myfefu.domain.repository.SessionManager
import ru.jone501.myfefu.ui.lucide.ExternLink
import ru.jone501.myfefu.ui.theme.MontserratAlternates
import ru.jone501.myfefu.utils.abbreviated
import ru.jone501.myfefu.utils.academicWeekNumber
import ru.jone501.myfefu.utils.getMainLocale
import ru.jone501.myfefu.utils.getStartOfWeek
import ru.jone501.myfefu.utils.swapIfRu
import ru.jone501.myfefu.utils.toStringWithMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun SchedulePage(
    bottomPadding: Dp,
    lessonsViewModel: LessonsViewModel = koinViewModel(),
    sessionManager: SessionManager = koinInject()
) {
    val pagesCount = Int.MAX_VALUE
    val initialPage = pagesCount / 2
    val pagerState = rememberPagerState(initialPage) { pagesCount }

    val today = LocalDate.now()

    val selectedWeekDelta = rememberSaveable { mutableIntStateOf(0) }
    val selectedDayOfWeek = rememberSaveable { mutableStateOf(today.dayOfWeek) }

    val selectedGroup: MutableState<Int?> = rememberSaveable { mutableStateOf(null) }
    val selectedSubgroup: MutableState<String?> = rememberSaveable { mutableStateOf(null) }

    val lessons by lessonsViewModel.lessons.collectAsState()
    val groups: MutableState<Map<Int, String>?> = rememberSaveable { mutableStateOf(null) }
    val subgroups by lessonsViewModel.subgroups.collectAsState()

    LaunchedEffect("loadSelectedGroup") {
        sessionManager.getSelectedGroup().collect {
            if (it != null) selectedGroup.value = it
        }
    }
    LaunchedEffect("onChangeSelectedGroup") {
        snapshotFlow { selectedGroup.value }.collect {
            if (it != null) sessionManager.setSelectedGroup(it)
        }
    }
    LaunchedEffect("initSelectedGroupAfterLoad") {
        sessionManager.getStudentProfileInfo().collect {
            groups.value = it?.associate { x -> x.academicGroupId to x.academicGroup }
            if (selectedGroup.value == null)
                selectedGroup.value = it?.minByOrNull { x -> x.academicGroupId }?.academicGroupId
        }
    }

    LaunchedEffect("loadSelectedSubgroup") {
        sessionManager.getSelectedSubgroup().collect {
            if (it != null && selectedGroup.value != null) selectedSubgroup.value = it[selectedGroup.value]
        }
    }
    LaunchedEffect("onChangeSelectedSubgroup") {
        snapshotFlow { selectedSubgroup.value }.collect {
            if (it != null && selectedGroup.value != null) sessionManager.setSelectedSubgroup(selectedGroup.value!!, it)
        }
    }
    LaunchedEffect("initSelectedSubgroupAfterLoad") {
        lessonsViewModel.subgroups.collect {
            if (selectedSubgroup.value == null && selectedGroup.value != null)
                selectedSubgroup.value = it[selectedGroup.value]?.minOrNull()
        }
    }

    LaunchedEffect("selectedWeekDelta") {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            selectedWeekDelta.intValue = page - initialPage
        }
    }

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            modifier = Modifier
                .padding(0.dp, 25.dp, 0.dp, 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(25.dp, 0.dp)
            ) {
                Text(
                    stringResource(R.string.schedule).uppercase(),
                    fontSize = 24.sp,
                    fontFamily = MontserratAlternates,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row {
                    val selectedDay = today.getStartOfWeek()
                        .plusWeeks(selectedWeekDelta.intValue.toLong())
                        .plusDays(selectedDayOfWeek.value.value - 1L)
                    val academicWeekNumber = selectedDay.academicWeekNumber()
                    Text(
                        "${selectedDay.toStringWithMonth(LocalContext.current)} ${selectedDay.year}",
                        fontSize = 16.sp,
                        fontFamily = MontserratAlternates,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (academicWeekNumber != null) {
                        Text(
                            " - ",
                            fontSize = 16.sp,
                            fontFamily = MontserratAlternates,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                        Text(
                            "${
                                swapIfRu(
                                    stringResource(R.string.week).lowercase(),
                                    academicWeekNumber,
                                    LocalContext.current.getMainLocale()
                                )
                            } (${
                                (if (academicWeekNumber % 2 == 0)
                                    stringResource(R.string.even)
                                else stringResource(R.string.odd)).lowercase()
                            })",
                            fontSize = 16.sp,
                            fontFamily = MontserratAlternates,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                AnimatedVisibility(groups.value != null && groups.value!!.size > 1) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(0.dp, 10.dp, 0.dp, 0.dp)
                            .fillMaxWidth()
                    ) {
                        GroupsSelector(selectedGroup, groups.value)
                    }
                }
                AnimatedVisibility(
                    selectedGroup.value != null && (subgroups[selectedGroup.value]?.isEmpty()
                        ?.not() ?: false)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(0.dp, 10.dp, 0.dp, 0.dp)
                            .fillMaxWidth()
                    ) {
                        SubgroupsSelector(selectedSubgroup, subgroups[selectedGroup.value])
                    }
                }
            }
            WeekPager(
                pagerState,
                initialPage,
                today,
                selectedDayOfWeek,
                selectedGroup,
                selectedSubgroup,
                lessons,
                bottomPadding
            )
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

@Composable
fun WeekPager(
    pagerState: PagerState,
    initialPage: Int,
    today: LocalDate,
    selectedDayOfWeek: MutableState<DayOfWeek>,
    selectedGroup: MutableState<Int?>,
    selectedSubgroup: MutableState<String?>,
    lessons: Map<Int, Map<String, Map<LocalDate, List<Lesson>>>?>,
    bottomPadding: Dp
) {
    val todayWeek = today.getStartOfWeek()

    HorizontalPager(pagerState) { page ->
        val weekDelta = page - initialPage
        val currentWeek = todayWeek.plusWeeks(weekDelta.toLong())

        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            modifier = Modifier
                .padding(25.dp, 0.dp)
        ) {
            DaysRow((0L..5).map {
                currentWeek.plusDays(it)
            }, selectedDayOfWeek)
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LessonList(
                    currentWeek.plusDays(selectedDayOfWeek.value.value - 1L),
                    lessons,
                    selectedSubgroup,
                    selectedGroup,
                    bottomPadding
                )
            }
        }
    }
}

@Composable
fun DaysRow(dates: List<LocalDate>, selectedDayOfWeek: MutableState<DayOfWeek>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        for (date in dates) {
            DayElement(date, selectedDayOfWeek)
        }
    }
}

@Composable
fun RowScope.DayElement(date: LocalDate, selectedDayOfWeek: MutableState<DayOfWeek>) {
    val interactionSource = remember { MutableInteractionSource() }
    val selected = date.dayOfWeek == selectedDayOfWeek.value
    val backgroundColor = animateColorAsState(
        if (selected) MaterialTheme.colorScheme.surfaceTint
        else MaterialTheme.colorScheme.surface
    )
    val outlineColor = animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onBackground
        else MaterialTheme.colorScheme.onBackground.copy(0f)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                onClick = {
                    selectedDayOfWeek.value = date.dayOfWeek
                },
                interactionSource = interactionSource,
                indication = null
            )
            .border(
                BorderStroke(2.dp, outlineColor.value),
                CircleShape
            )
            .background(
                backgroundColor.value,
                CircleShape
            )
            .weight(1f)
            .padding(0.dp, 10.dp)
    ) {
        Text(
            date.dayOfWeek.abbreviated(LocalContext.current),
            fontSize = 14.sp,
            fontFamily = MontserratAlternates,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "${date.dayOfMonth}",
            fontSize = 18.sp,
            fontFamily = MontserratAlternates,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun GroupsSelector(selectedGroup: MutableState<Int?>, groups: Map<Int, String>?) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .background(
                MaterialTheme.colorScheme.surface,
                CircleShape
            )
            .clickable(
                onClick = {
                    if (groups != null && groups.size > 1)
                        showDialog = true
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(10.dp, 1.dp)
    ) {
        Text(
            stringResource(R.string.group),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = MontserratAlternates,
            modifier = Modifier.padding(0.dp, 2.dp)
        )
        AnimatedVisibility(selectedGroup.value != null) {
            Row {
                Spacer(Modifier.width(5.dp))
                Text(
                    selectedGroup.value.let { groups?.get(it) ?: "-" },
                    fontFamily = MontserratAlternates,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(0.dp, 2.dp)
                )
            }
        }
    }
    if (showDialog) {
        GroupSelectionDialog(selectedGroup, groups!!) { showDialog = false }
    }
}

@Composable
fun LessonInfoDialog(lesson: Lesson, onDismiss: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background.copy(0.5f))
                .clickable(
                    onClick = onDismiss,
                    indication = null,
                    interactionSource = interactionSource
                )
                .padding(25.dp)
                .fillMaxSize()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .clickable(
                        onClick = { },
                        indication = null,
                        interactionSource = interactionSource
                    )
                    .dropShadow(
                        RoundedCornerShape(25.dp),
                        Shadow(
                            25.dp,
                            MaterialTheme.colorScheme.background,
                            10.dp
                        )
                    )
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(25.dp)
                    )
                    .fillMaxWidth()
                    .padding(15.dp)
            ) {
                Column {
                    Text(
                        "${stringResource(R.string.discipline)}:",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = MontserratAlternates
                    )
                    Text(
                        lesson.discipline.name,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MontserratAlternates
                    )
                }
                if (lesson.facility != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(top = 5.dp)
                    ) {
                        Text(
                            "${stringResource(R.string.classroom)}:",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = MontserratAlternates
                        )
                        Text(
                            lesson.facility!!.name,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MontserratAlternates
                        )
                    }
                }
                Column(
                    Modifier.padding(top = 5.dp)
                ) {
                    Text(
                        "${stringResource(R.string.lesson_type)}:",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = MontserratAlternates
                    )
                    Text(
                        lesson.ppsLoad.name,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MontserratAlternates
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(top = 5.dp)
                ) {
                    Text(
                        "${stringResource(R.string.time)}:",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = MontserratAlternates
                    )
                    Text(
                        "${lesson.start_time.format(formatter)} - ${lesson.end_time.format(formatter)}",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = MontserratAlternates
                    )
                }
                if (lesson.teacher != null) {
                    Column(
                        Modifier.padding(top = 5.dp)
                    ) {
                        Text(
                            "${stringResource(R.string.teacher)}:",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = MontserratAlternates
                        )
                        Text(
                            lesson.teacher!!.fullName,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MontserratAlternates
                        )
                    }
                }
                if (lesson.distance_education_description.isNotEmpty()) {
                    Column(
                        Modifier.padding(top = 5.dp)
                    ) {
                        Text(
                            "${stringResource(R.string.distance_education_desc)}:",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = MontserratAlternates
                        )
                        Text(
                            lesson.distance_education_description,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MontserratAlternates
                        )
                    }
                }
                if (lesson.distance_education_url.isNotEmpty()) {
                    val uriHandler = LocalUriHandler.current

                    Row(
                        Modifier
                            .padding(top = 5.dp)
                            .clip(CircleShape)
                            .clickable(onClick = {
                                uriHandler.openUri(lesson.distance_education_url)
                            })
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceTint)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.5.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.distance_education_link),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = MontserratAlternates
                        )
                        Image(
                            imageVector = ExternLink(MaterialTheme.colorScheme.onBackground, 3f),
                            contentDescription = "Distance education link",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GroupSelectionDialog(selectedGroup: MutableState<Int?>, groups: Map<Int, String>, onDismiss: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background.copy(0.5f))
                .clickable(
                    onClick = onDismiss,
                    indication = null,
                    interactionSource = interactionSource
                )
                .padding(50.dp)
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(
                        onClick = { },
                        indication = null,
                        interactionSource = interactionSource
                    )
                    .dropShadow(
                        RoundedCornerShape(25.dp),
                        Shadow(
                            25.dp,
                            MaterialTheme.colorScheme.background,
                            10.dp
                        )
                    )
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(25.dp)
                    )
            ) {
                Text(
                    stringResource(R.string.select_group),
                    fontFamily = MontserratAlternates,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(15.dp, 5.dp)
                )
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(23.dp)
                        )
                        .padding(15.dp, 10.dp)
                ) {
                    items(groups.toList()) { (groupId, group) ->
                        Text(
                            group,
                            fontFamily = MontserratAlternates,
                            fontSize = 18.sp,
                            fontWeight = if (groupId == selectedGroup.value) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (groupId == selectedGroup.value) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(
                                    onClick = {
                                        selectedGroup.value = groupId
                                        onDismiss()
                                    },
                                    indication = null,
                                    interactionSource = interactionSource
                                )
                                .padding(5.dp, 0.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubgroupsSelector(selectedSubgroup: MutableState<String?>, subgroups: Set<String>?) {
    var subgroupMenuExpanded by remember { mutableStateOf(false) }
    val backgroundColor = animateColorAsState(
        if (subgroupMenuExpanded) MaterialTheme.colorScheme.surfaceTint
        else MaterialTheme.colorScheme.surface
    )
    val outlineColor = animateColorAsState(
        if (subgroupMenuExpanded) MaterialTheme.colorScheme.onBackground
        else MaterialTheme.colorScheme.onBackground.copy(0f)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(
                onClick = {
                    if (!subgroupMenuExpanded) {
                        if (subgroups?.isEmpty() ?: true)
                            return@clickable
                    }
                    subgroupMenuExpanded = !subgroupMenuExpanded
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
            .padding(10.dp, 1.dp, 1.dp, 1.dp)
    ) {
        Text(
            stringResource(R.string.subgroup),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = MontserratAlternates,
            modifier = Modifier.padding(0.dp, 2.dp)
        )
        AnimatedVisibility(selectedSubgroup.value != null) {
            Row {
                Spacer(Modifier.width(10.dp))
                Text(
                    selectedSubgroup.value ?: "-",
                    fontFamily = MontserratAlternates,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(0.dp, 2.dp)
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        AnimatedVisibility(subgroupMenuExpanded) {
            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(25.dp))
            ) {
                if (subgroups != null) {
                    for (subgroup in subgroups) {
                        if (selectedSubgroup.value == subgroup)
                            continue
                        item {
                            Text(
                                subgroup,
                                fontFamily = MontserratAlternates,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(12.5.dp, 2.dp)
                                    .clickable(
                                        onClick = {
                                            if (!transition.isRunning) {
                                                subgroupMenuExpanded = false
                                                selectedSubgroup.value = subgroup
                                            }
                                        }, indication = null,
                                        interactionSource = remember { MutableInteractionSource() })
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LessonList(
    date: LocalDate,
    lessons: Map<Int, Map<String, Map<LocalDate, List<Lesson>>>?>,
    selectedSubgroup: MutableState<String?>,
    selectedGroup: MutableState<Int?>,
    bottomPadding: Dp,
    preferences: AppPreferences = koinViewModel()
) {
    val isWindowsEnabled by preferences.windowsEnabled.collectAsState()
    val viewLesson: MutableState<Lesson?> = remember { mutableStateOf(null) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(25.dp, 25.dp, 0.dp, 0.dp))
            .fillMaxSize()
    ) {
        if (selectedGroup.value != null && selectedSubgroup.value != null && lessons[selectedGroup.value]?.get(selectedSubgroup.value)?.isEmpty()?.not() ?: false) {
            val currentLessons = lessons[selectedGroup.value]?.get(selectedSubgroup.value)?.get(date)
            if (currentLessons != null) {
                items(currentLessons.filter { it !is EmptyLesson || isWindowsEnabled },
                    key = { if (it is EmptyLesson) "WINDOW:${it.start_time}" else it.id }) { lesson ->
                    if (lesson is EmptyLesson) {
                        EmptyLessonElement(
                            lesson.start_time,
                            lesson.end_time
                        )
                    } else {
                        LessonElement(
                            viewLesson,
                            lesson,
                            lesson.start_time,
                            lesson.end_time,
                            lesson.ppsLoad.name,
                            facilityPrettier(lesson.facility?.name),
                            lesson.discipline.name
                        )
                    }
                }
                item {
                    Spacer(
                        Modifier
                            .height(bottomPadding + 65.dp)
                    )
                }
            } else {
                item {
                    Text(
                        stringResource(R.string.no_lessons),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = MontserratAlternates,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
    if (viewLesson.value != null) {
        LessonInfoDialog(viewLesson.value!!) { viewLesson.value = null }
    }
}

@Composable
fun LazyItemScope.LessonElement(viewLesson: MutableState<Lesson?>, lesson: Lesson, startTime: LocalDateTime, endTime: LocalDateTime, type: String, facility: String?, discipline: String) {
    val nowDateTime = LocalDateTime.now()
    val isNow = startTime.toLocalDate() == nowDateTime.toLocalDate()
            && startTime.isBefore(nowDateTime)
            && endTime.isAfter(nowDateTime)
    val backgroundColor = animateColorAsState(
        if (isNow) MaterialTheme.colorScheme.surfaceTint
        else MaterialTheme.colorScheme.surface
    )
    val outlineColor = animateColorAsState(
        if (isNow) MaterialTheme.colorScheme.onBackground
        else MaterialTheme.colorScheme.onBackground.copy(0f)
    )
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .clip(
                RoundedCornerShape(25.dp)
            )
            .clickable(onClick = {
                viewLesson.value = lesson
            })
            .background(
                backgroundColor.value,
                RoundedCornerShape(25.dp)
            )
            .border(
                BorderStroke(2.dp, outlineColor.value),
                RoundedCornerShape(25.dp)
            )
            .fillMaxWidth()
            .padding(15.dp)
            .animateItem()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                "${startTime.format(formatter)} - ${endTime.format(formatter)}",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = MontserratAlternates,
                lineHeight = 1.sp
            )
            Text(
                type,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = MontserratAlternates,
                lineHeight = 1.sp
            )
        }
        if (facility != null) Text(
            facility,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = MontserratAlternates
        )
        Text(
            discipline,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = MontserratAlternates
        )
    }
}

@Composable
fun LazyItemScope.EmptyLessonElement(startTime: LocalDateTime, endTime: LocalDateTime) {
    val nowDateTime = LocalDateTime.now()
    val isNow = startTime.toLocalDate() == nowDateTime.toLocalDate()
            && startTime.isBefore(nowDateTime)
            && endTime.isAfter(nowDateTime)
    val outlineColor = animateColorAsState(
        if (isNow) MaterialTheme.colorScheme.onBackground
        else MaterialTheme.colorScheme.surface
    )
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    Box(
        Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .animateItem()
    ) {
        Canvas(
            modifier = Modifier
                .padding(2.dp)
                .matchParentSize()
        ) {
            val strokeWidth = 4.dp.toPx()
            val dashOnLength = 10.dp.toPx()
            val dashOffLength = 20.dp.toPx()
            val cornerRadius = 25.dp.toPx()

            val dashPathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(dashOnLength, dashOffLength),
                phase = 0f
            )

            drawRoundRect(
                color = outlineColor.value,
                style = Stroke(
                    width = strokeWidth,
                    join = StrokeJoin.Round,
                    cap = StrokeCap.Round,
                    pathEffect = dashPathEffect
                ),
                cornerRadius = CornerRadius(cornerRadius)
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .padding(15.dp)
        ) {
            Text(
                "${startTime.format(formatter)} - ${endTime.format(formatter)}",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = MontserratAlternates,
                lineHeight = 1.sp
            )
            Text(
                stringResource(R.string.window),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = MontserratAlternates
            )
        }
    }
}