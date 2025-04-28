package com.example.cocktailapp

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.min
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

@Composable
fun TimerScreen(
    timerViewModel: TimerViewModel,
    drinkId: String,
    preparationTime: Int,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(drinkId) {
        timerViewModel.loadDurationForDrink(drinkId, preparationTime)
    }

    val timeLeft = timerViewModel.getTimeLeft(drinkId)
    val isRunning = timerViewModel.isRunning(drinkId)
    val selectedDuration = timerViewModel.selectedDuration(drinkId)

    val minutes = (timeLeft / 1000 / 60).toInt()
    val seconds = (timeLeft / 1000 % 60).toInt()
    val totalDuration = selectedDuration

    val progress = if (totalDuration != 0L) timeLeft.toFloat() / totalDuration else 1f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        TimerCircle(
            minutes = minutes,
            seconds = seconds,
            progress = animatedProgress
        )

        if (!isRunning && timeLeft == selectedDuration) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                iOSPicker(
                    label = "Minuty",
                    range = 0..59,
                    selectedValue = minutes,
                    onValueChange = { newMinutes ->
                        timerViewModel.setMinutes(drinkId, newMinutes)
                    }
                )
                Spacer(Modifier.width(32.dp))
                iOSPicker(
                    label = "Sekundy",
                    range = 0..59,
                    selectedValue = seconds,
                    onValueChange = { newSeconds ->
                        timerViewModel.setSeconds(drinkId, newSeconds)
                    }
                )
            }
        }

        Row(
            modifier = Modifier
                //.fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            //horizontalArrangement = Arrangement.spacedBy(8.dp),
            //verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TimerButton(
                icon = Icons.Default.PlayArrow,
                contentDescription = "Start",
                enabled = !isRunning,
                onClick = {
                    timerViewModel.startTimer(drinkId)
                }
            )
            //Spacer(Modifier.width(16.dp))
            TimerButton(
                icon = Icons.Default.Pause,
                contentDescription = "Pauza",
                enabled = isRunning,
                onClick = {
                    timerViewModel.pauseTimer(drinkId)
                }
            )
            //Spacer(Modifier.width(16.dp))
            TimerButton(
                icon = Icons.Default.Stop,
                contentDescription = "Reset",
                enabled = true,
                onClick = {
                    timerViewModel.resetTimer(drinkId)
                }
            )
            //Spacer(Modifier.width(16.dp)) // <-- dodaj odstęp
            TimerButton(
                icon = Icons.Default.Refresh, // <-- użyj ikony odświeżania
                contentDescription = "Przywróć czas przygotowania",
                enabled = true,
                onClick = {
                    timerViewModel.resetToPreparationTime(drinkId, preparationTime)
                }
            )
        }
    }
}

@Composable
fun TimerCircle(minutes: Int, seconds: Int, progress: Float) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(250.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()
            val diameter = min(size.width, size.height) - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            drawArc(
                color = Color.LightGray,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
            )

            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            style = MaterialTheme.typography.displayMedium
        )
    }
}

@Composable
fun iOSPicker(
    label: String,
    range: IntRange,
    selectedValue: Int,
    onValueChange: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    val itemHeight = 48.dp

    LaunchedEffect(selectedValue) {
        val index = selectedValue - range.first
        listState.animateScrollToItem(index)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 22.sp,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .padding(top = 16.dp, bottom = 16.dp)
        )
        Box(
            modifier = Modifier
                .height(itemHeight * 3)
                .width(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                )
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(vertical = itemHeight),
                horizontalAlignment = Alignment.CenterHorizontally,
                flingBehavior = rememberSnapFlingBehavior(listState),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(range.toList()) { _, number ->
                    Text(
                        text = number.toString().padStart(2, '0'),
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight)
                            .wrapContentHeight(Alignment.CenterVertically),
                        maxLines = 1
                    )
                }
            }
            Box(
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(itemHeight)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = MaterialTheme.shapes.small
                    )
            )
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collectLatest { isScrolling ->
                if (!isScrolling) {
                    val centeredIndex = listState.firstVisibleItemIndex
                    val newValue = range.first + centeredIndex
                    if (newValue in range) {
                        onValueChange(newValue)
                    }
                }
            }
    }
}

@Composable
fun TimerButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(36.dp)
        )
    }
}
