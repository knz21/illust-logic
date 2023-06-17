package com.kenzo.logicbase

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.pow

@Composable
fun DirectionalController(
    paintMode: CellState,
    onDirectionPress: (direction: Direction) -> Unit,
    onPaintButtonPress: () -> Unit,
    onPaintButtonRemove: () -> Unit,
    onCheckButtonPress: () -> Unit,
    onCheckButtonRemove: () -> Unit
) {
    val view = LocalView.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DirectionalPad(
            modifier = Modifier.weight(1f),
            onTap = {
                onDirectionPress(it)
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier.weight(4f)
            ) {
                Spacer(modifier = Modifier.weight(2f))
                PaintButton(
                    modifier = Modifier.weight(4f),
                    paintMode = CellState.Checked,
                    onPress = onCheckButtonPress,
                    onRemove = onCheckButtonRemove
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier.weight(4f)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                PaintButton(
                    modifier = Modifier.weight(4f),
                    paintMode = CellState.Painted,
                    onPress = onPaintButtonPress,
                    onRemove = onPaintButtonRemove
                )
                Spacer(modifier = Modifier.weight(2f))
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun DirectionalPad(
    modifier: Modifier,
    onTap: (direction: Direction) -> Unit
) {
    var direction by remember { mutableStateOf(Direction.None) }
    var isPressed by remember { mutableStateOf(false) }
    var repeatCount by remember { mutableStateOf(0) }
    var controllerSize by remember { mutableStateOf(0.01f) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(9999.dp))
            .background(Color(0xFF_DDDDDD))
    ) {
        LaunchedEffect(isPressed) {
            if (!isPressed || direction == Direction.None) return@LaunchedEffect
            onTap(direction)
            repeatCount = 0
            while (isPressed) {
                delay(calculateRepeatDelay(repeatCount++))
                onTap(direction)
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput("tap") {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            val x = it.x - controllerSize / 2
                            val y = it.y - controllerSize / 2
                            direction = when {
                                y > x && y > -x -> Direction.Down
                                y > x && y < -x -> Direction.Left
                                y < x && y < -x -> Direction.Up
                                else -> Direction.Right
                            }
                            try {
                                awaitRelease()
                            } finally {
                                isPressed = false
                            }
                        }
                    )
                }
        ) {
            controllerSize = size.width
            drawLine(
                color = Color(0xFF_333333),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height),
                strokeWidth = 3f
            )
            drawLine(
                color = Color(0xFF_333333),
                start = Offset(size.width, 0f),
                end = Offset(0f, size.height),
                strokeWidth = 3f
            )
        }
    }
}

enum class Direction {
    Up,
    Down,
    Left,
    Right,
    None
}

private fun calculateRepeatDelay(repeatCount: Int): Long {
    val initialDelay = 250L
    val minDelay = 90L
    val accelerationFactor = 0.8
    return (initialDelay * accelerationFactor.pow(repeatCount).toLong()).coerceAtLeast(minDelay)
}

@Composable
private fun PaintButton(
    modifier: Modifier,
    paintMode: CellState,
    onPress: () -> Unit,
    onRemove: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(9999.dp))
            .background(if (isPressed) Color(0xFF_999999) else Color(0xFF_DDDDDD))
            .pointerInput("tap") {
                detectTapGestures(
                    onPress = {
                        try {
                            onPress()
                            isPressed = true
                            awaitRelease()
                        } finally {
                            onRemove()
                            isPressed = false
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        SampleCell(
            modifier = Modifier
                .fillMaxSize()
                .scale(0.5f),
            state = paintMode
        )
    }
}

@Composable
fun PaintModeController(
    modifier: Modifier,
    paintMode: CellState,
    onPaintModeChange: (paintMode: CellState) -> Unit
) {
    Row(
        modifier = modifier.padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SampleCell(
            modifier = Modifier.size(40.dp),
            state = CellState.Checked
        )
        Switch(
            checked = paintMode == CellState.Painted,
            onCheckedChange = {
                onPaintModeChange(if (it) CellState.Painted else CellState.Checked)
            }
        )
        SampleCell(
            modifier = Modifier.size(40.dp),
            state = CellState.Painted
        )
    }
}

@Composable
fun SampleCell(
    modifier: Modifier,
    state: CellState
) {
    Canvas(
        modifier = modifier
    ) {
        when (state) {
            CellState.Empty -> {
                drawSquareStroke(
                    color = Color(0xFF_333333),
                    size = size.width,
                    strokeWidth = 3f
                )
            }

            CellState.Painted -> {
                drawRect(
                    color = Color(0xFF_333333)
                )
            }

            CellState.Checked -> {
                drawSquareStroke(
                    color = Color(0xFF_333333),
                    size = size.width,
                    strokeWidth = 3f
                )
                drawLine(
                    color = Color(0xFF_333333),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 3f
                )
                drawLine(
                    color = Color(0xFF_333333),
                    start = Offset(size.width, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 3f
                )
            }
        }
    }
}

@Composable
fun ControllerSwitch(
    modifier: Modifier,
    touchMode: Boolean,
    onTouchModeChange: (touchMode: Boolean) -> Unit
) {
    Row(
        modifier = modifier.padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(40.dp),
            painter = painterResource(R.drawable.twotone_videogame_asset_24),
            contentDescription = null
        )
        Switch(
            checked = touchMode,
            onCheckedChange = { onTouchModeChange(it) }
        )
        Icon(
            modifier = Modifier.size(40.dp),
            painter = painterResource(R.drawable.twotone_touch_app_24),
            contentDescription = null
        )
    }
}