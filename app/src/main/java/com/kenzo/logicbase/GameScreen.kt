package com.kenzo.logicbase

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun GameScreen(
    cells: List<Cell>,
    nextGame: () -> Unit,
    updateData: (data : List<Cell>) -> Unit,
    forceUpdateData: (row: Int, col: Int, paintMode: CellState) -> CellState,
    successiveUpdateData: (row: Int, col: Int, state: CellState, paintMode: CellState) -> Unit
) {
    var changingTo by remember { mutableStateOf(CellState.Empty) }
    var paintMode by remember { mutableStateOf(CellState.Painted) }
    var isSuccessive by remember { mutableStateOf(false) }
    var screenWidth by remember { mutableStateOf(0) }
    var selectedRowIndex by remember { mutableStateOf(-1) }
    var selectedColumnIndex by remember { mutableStateOf(-1) }
    var touchMode by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            .onSizeChanged { screenWidth = it.width }
    ) {
        Display(data = cells) {
            Field(
                data = cells,
                startPaint = { row, col ->
                    if (touchMode) {
                        changingTo = forceUpdateData(row, col, paintMode)
                    } else {
                        selectedRowIndex = row
                        selectedColumnIndex = col
                    }
                },
                paint = { row, col ->
                    if (touchMode) {
                        successiveUpdateData(row, col, changingTo, paintMode)
                    } else {
                        selectedRowIndex = row
                        selectedColumnIndex = col
                    }
                }
            )
            Selection(
                data = cells,
                selectedRowIndex = selectedRowIndex,
                selectedColumnIndex = selectedColumnIndex
            )
        }
        ControllerSwitch(
            modifier = Modifier.align(Alignment.End),
            touchMode = touchMode,
            onTouchModeChange = {
                touchMode = it
                selectedRowIndex = -1
                selectedColumnIndex = -1
            }
        )
        if (touchMode) {
            PaintModeController(
                Modifier.align(Alignment.End),
                paintMode = paintMode,
                onPaintModeChange = { paintMode = it }
            )
        } else {
            DirectionalController(
                paintMode,
                onDirectionPress = {
                    if (selectedColumnIndex == -1 || selectedRowIndex == -1) {
                        selectedColumnIndex = 0
                        selectedRowIndex = 0
                    } else {
                        val maxIndex = cells.rowCount() - 1
                        when (it) {
                            Direction.Up -> {
                                selectedRowIndex = if (selectedRowIndex > 0) selectedRowIndex - 1 else maxIndex
                            }

                            Direction.Down -> {
                                selectedRowIndex = if (selectedRowIndex < maxIndex) selectedRowIndex + 1 else 0
                            }

                            Direction.Left -> {
                                selectedColumnIndex = if (selectedColumnIndex > 0) selectedColumnIndex - 1 else maxIndex
                            }

                            Direction.Right -> {
                                selectedColumnIndex = if (selectedColumnIndex < maxIndex) selectedColumnIndex + 1 else 0
                            }

                            else -> {}
                        }
                    }
                    if (isSuccessive) {
                        successiveUpdateData(selectedRowIndex, selectedColumnIndex, changingTo, paintMode)
                    }
                },
                onPaintButtonPress = {
                    paintMode = CellState.Painted
                    changingTo = forceUpdateData(selectedRowIndex, selectedColumnIndex, paintMode)
                    isSuccessive = true
                },
                onPaintButtonRemove = {
                    changingTo = CellState.Empty
                    isSuccessive = false
                },
                onCheckButtonPress = {
                    paintMode = CellState.Checked
                    changingTo = forceUpdateData(selectedRowIndex, selectedColumnIndex, paintMode)
                    isSuccessive = true
                },
                onCheckButtonRemove = {
                    changingTo = CellState.Empty
                    isSuccessive = false
                }
            )
        }
        ClearDialog(
            cleared = cells.all {
                it.state == CellState.Painted && it.answer || it.state != CellState.Painted && !it.answer
            },
            onReset = {
                nextGame()
            }
        )
        DebugButtons(
            data = cells,
            updateData = updateData,
            resetSelected = {
                selectedRowIndex = -1
                selectedColumnIndex = -1
            }
        )
    }
}

@Composable
fun Display(
    data: List<Cell>,
    content: @Composable () -> Unit
) {
    var screenWidth by remember { mutableStateOf(0) }
    Column(
        Modifier
            .fillMaxWidth()
            .onSizeChanged { screenWidth = it.width }
    ) {
        var fieldSize by remember { mutableStateOf(0) }
        Row(
            Modifier.fillMaxWidth()
        ) {
            RowHintsSpace(data = data)
            ColumnHints(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { fieldSize = it.width },
                data = data
            )
        }
        Row(
            Modifier.fillMaxWidth()
        ) {
            RowHints(
                modifier = Modifier
                    .width(with(LocalDensity.current) { (screenWidth - fieldSize).toDp() })
                    .height(with(LocalDensity.current) { fieldSize.toDp() }),
                data = data
            )
            Box {
                content()
            }
        }
    }
}

@Composable
fun ColumnHints(
    modifier: Modifier,
    data: List<Cell>
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        data.toColumnHints().forEach {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                it.forEach { hint ->
                    HintNumber(hint = hint)
                }
            }
        }
    }
}

@Composable
fun RowHints(
    modifier: Modifier,
    data: List<Cell>
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        data.toRowHints().forEach {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                it.forEach { hint ->
                    HintNumber(hint = hint)
                }
            }
        }
    }
}

@Composable
fun RowHintsSpace(
    data: List<Cell>
) {
    Row(
        Modifier.alpha(0f)
    ) {
        repeat(data.toRowHints().maxOf { it.size }) {
            HintNumber(hint = 99)
        }
    }
}

@Composable
fun HintNumber(hint: Int) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .width(IntrinsicSize.Max)
            .padding(horizontal = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = hint.toString(),
            fontSize = with(density) { 12.dp.toSp() },
            fontWeight = FontWeight.Bold
        )
        Text(
            modifier = Modifier.alpha(0f),
            text = "00",
            fontSize = with(density) { 12.dp.toSp() },
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun Field(
    data: List<Cell>,
    startPaint: (row: Int, col: Int) -> Unit,
    paint: (row: Int, col: Int) -> Unit
) {
    var cellSize by remember { mutableStateOf(0.01f) }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput("drag") {
                detectDragGestures { change, _ ->
                    val row = (change.position.y / cellSize).toInt()
                    val col = (change.position.x / cellSize).toInt()
                    paint(row, col)
                }
            }
            .pointerInput("tap") {
                detectTapGestures(
                    onPress = {
                        val row = (it.y / cellSize).toInt()
                        val col = (it.x / cellSize).toInt()
                        startPaint(row, col)
                    }
                )
            }
    ) {
        cellSize = size.width / (maxOf(data.maxOf { it.rowIndex }, data.maxOf { it.colIndex }) + 1).toFloat()
        data.forEach {
            val offset = Offset(it.colIndex * cellSize, it.rowIndex * cellSize)
            drawRect(
                color = if (it.state == CellState.Painted) Color(0xFF000000) else Color(0xFFFFFFFF),
                topLeft = offset,
                size = Size(cellSize, cellSize)
            )
            if (it.state == CellState.Checked) {
                drawLine(
                    color = Color(0xFF000000),
                    start = offset,
                    end = Offset(offset.x + cellSize, offset.y + cellSize),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0xFF000000),
                    start = Offset(offset.x + cellSize, offset.y),
                    end = Offset(offset.x, offset.y + cellSize),
                    strokeWidth = 2f
                )
            }
        }
        (0..data.columnCount()).forEach {
            drawLine(
                color = Color(0xFF000000),
                start = Offset(it * cellSize, 0f),
                end = Offset(it * cellSize, size.width),
                strokeWidth = if (it % 5 == 0) 3f else 1f
            )
        }
        (0..data.rowCount()).forEach {
            drawLine(
                color = Color(0xFF000000),
                start = Offset(0f, it * cellSize),
                end = Offset(size.width, it * cellSize),
                strokeWidth = if (it % 5 == 0) 3f else 1f
            )
        }
    }
}

@Composable
fun Selection(
    data: List<Cell>,
    selectedRowIndex: Int,
    selectedColumnIndex: Int
) {
    val color = MaterialTheme.colorScheme.primary
    var cellSize by remember { mutableStateOf(0.01f) }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        cellSize = size.width / (maxOf(data.maxOf { it.rowIndex }, data.maxOf { it.colIndex }) + 1).toFloat()
        data.forEach {
            if (it.rowIndex == selectedRowIndex || it.colIndex == selectedColumnIndex) {
                val offset = Offset(it.colIndex * cellSize, it.rowIndex * cellSize)
                drawRect(
                    color = color.copy(alpha = 0.2f),
                    topLeft = offset,
                    size = Size(cellSize, cellSize)
                )
            }
        }
        data.forEach {
            if (it.rowIndex == selectedRowIndex && it.colIndex == selectedColumnIndex) {
                val strokeWidth = 5f
                drawSquareStroke(
                    color = color,
                    startOffset = Offset(
                        it.colIndex * cellSize - strokeWidth / 2,
                        it.rowIndex * cellSize - strokeWidth / 2
                    ),
                    size = cellSize + strokeWidth,
                    strokeWidth = strokeWidth
                )
            }
        }
    }
}

@Composable
fun ClearDialog(
    cleared: Boolean,
    onReset: () -> Unit
) {
    AnimatedVisibility(
        visible = cleared,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Dialog(
            onDismissRequest = onReset,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    Text(
                        text = "Cleared",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Button(onClick = onReset) {
                    Text(text = "Next")
                }
            }
        }
    }
}

@Composable
fun DebugButtons(
    data: List<Cell>,
    updateData: (List<Cell>) -> Unit,
    resetSelected: () -> Unit,
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Row {
            Button(onClick = { updateData(data.answered()) }) {
                Text("answer")
            }
            Button(onClick = { updateData(data.reset()) }) {
                Text("reset")
            }
            Button(onClick = resetSelected) {
                Text("unselect")
            }
        }
    }
}