package ru.jone501.myfefu.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.jone501.myfefu.R
import ru.jone501.myfefu.ui.theme.MontserratAlternates
import ru.jone501.myfefu.ui.theme.MyFEFUTheme
import ru.jone501.myfefu.utils.abbreviated
import ru.jone501.myfefu.utils.academicWeekNumber
import ru.jone501.myfefu.utils.getMainLocale
import ru.jone501.myfefu.utils.getStartOfWeek
import ru.jone501.myfefu.utils.swapIfRu
import ru.jone501.myfefu.utils.toStringWithMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
@Preview
fun SchedulePage() {
    val subgroups = (1..3).map { "$it" }
    val selectedSubgroup = rememberSaveable { mutableStateOf(subgroups[0]) }

    val pagesCount = Int.MAX_VALUE
    val initialPage = pagesCount / 2
    val pagerState = rememberPagerState(initialPage) { pagesCount }

    val today = LocalDate.now()
    val selectedWeekDelta = rememberSaveable { mutableIntStateOf(0) }
    val selectedDayOfWeek = rememberSaveable { mutableStateOf(today.dayOfWeek) }

    LaunchedEffect(null) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            selectedWeekDelta.intValue = page - initialPage
        }
    }

    MyFEFUTheme(darkTheme = true) {
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
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            if (academicWeekNumber != null) " - ${
                                swapIfRu(
                                    stringResource(R.string.week).lowercase(),
                                    academicWeekNumber,
                                    LocalContext.current.getMainLocale()
                                )
                            } (${
                                (if (academicWeekNumber % 2 == 0)
                                    stringResource(R.string.even)
                                else stringResource(R.string.odd)).lowercase()
                            })" else "",
                            fontSize = 16.sp,
                            fontFamily = MontserratAlternates,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    SubgroupsSelector(selectedSubgroup, subgroups)
                }
                WeekPager(pagerState, initialPage, today, selectedDayOfWeek)
            }
            Box(
                Modifier
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background.copy(0f),
                            ),
                            start = Offset(0f, 200f),
                            end = Offset(0f, 0f)
                        )
                    )
                    .height(75.dp)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun WeekPager(pagerState: PagerState, initialPage: Int, today: LocalDate, selectedDayOfWeek: MutableState<DayOfWeek>) {
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
//                Text(
//                    "Расписание могло измениться. Подключитесь к интернету, чтобы проверить обновления.",
//                    color = MaterialTheme.colorScheme.onSurface,
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.Light,
//                    fontFamily = MontserratAlternates,
//                    textAlign = TextAlign.Center,
//                    lineHeight = 14.sp,
//                    modifier = Modifier.padding(15.dp, 0.dp)
//                )
                LessonList(currentWeek.plusDays(selectedDayOfWeek.value.value - 1L))
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
fun SubgroupsSelector(selectedSubgroup: MutableState<String>, subgroups: List<String>) {
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
                    subgroupMenuExpanded = !subgroupMenuExpanded
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .background(
                backgroundColor.value,
                RoundedCornerShape(25.dp)
            )
            .border(
                BorderStroke(2.dp, outlineColor.value),
                RoundedCornerShape(25.dp)
            )
            .padding(10.dp, 1.dp, 1.dp, 1.dp)
    ) {
        Text(
            "${stringResource(R.string.subgroup)}:",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = MontserratAlternates,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            selectedSubgroup.value,
            fontFamily = MontserratAlternates,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(0.dp, 2.dp)
        )
        Spacer(Modifier.width(10.dp))
        AnimatedVisibility(subgroupMenuExpanded) {
            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(25.dp))
//                    .background(MaterialTheme.colorScheme.surface)
//                    .background(backgroundColor.value)
            ) {
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

@Composable
fun LessonList(date: LocalDate) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(25.dp, 25.dp, 0.dp, 0.dp))
            .fillMaxWidth()
    ) {
        item {
            LessonElement(
                LocalDateTime.of(date, LocalTime.of(10, 0)),
                LocalDateTime.of(date, LocalTime.of(11, 30)),
                "Лекционное занятие",
                "D456",
                "Линейная алгебра"
            )
        }
        item {
            LessonElement(
                LocalDateTime.of(date, LocalTime.of(11, 40)),
                LocalDateTime.of(date, LocalTime.of(13, 10)),
                "Лекционное занятие",
                "D619",
                "Основы российской государственности"
            )
        }
        item {
            LessonElement(
                LocalDateTime.of(date, LocalTime.of(13, 20)),
                LocalDateTime.of(date, LocalTime.of(14, 50)),
                "Практическое занятие",
                "D733",
                "Математический анализ"
            )
        }
        item {
            LessonElement(
                LocalDateTime.of(date, LocalTime.of(13, 20)),
                LocalDateTime.of(date, LocalTime.of(14, 50)),
                "Практическое занятие",
                "D733",
                "Математический анализ"
            )
        }
        item {
            LessonElement(
                LocalDateTime.of(date, LocalTime.of(13, 20)),
                LocalDateTime.of(date, LocalTime.of(14, 50)),
                "Практическое занятие",
                "D733",
                "Математический анализ"
            )
        }
        item {
            LessonElement(
                LocalDateTime.of(date, LocalTime.of(13, 20)),
                LocalDateTime.of(date, LocalTime.of(14, 50)),
                "Практическое занятие",
                "D733",
                "Математический анализ"
            )
        }
        item {
            Spacer(
                Modifier
                    .height(80.dp)
            )
        }
    }
}

@Composable
fun LessonElement(startTime: LocalDateTime, endTime: LocalDateTime, type: String, facility: String?, discipline: String) {
    val nowDateTime = LocalDateTime.of(2026, 9, 25, 12, 0)
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