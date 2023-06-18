package com.kenzo.logicbase

import android.util.Log
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
import androidx.compose.runtime.rememberUpdatedState
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
    logicData: List<List<Boolean>>,
    nextGame: () -> Unit
) {
    val originalData by rememberUpdatedState(newValue = Cell.fromLogicData(logicData))
    var data by remember { mutableStateOf(originalData) }
    var changingTo by remember { mutableStateOf(CellState.Empty) }
    var paintMode by remember { mutableStateOf(CellState.Painted) }
    var isSuccessive by remember { mutableStateOf(false) }
    var screenWidth by remember { mutableStateOf(0) }
    var selectedRowIndex by remember { mutableStateOf(-1) }
    var selectedColumnIndex by remember { mutableStateOf(-1) }
    var touchMode by remember { mutableStateOf(false) }
    var cleared by remember { mutableStateOf(false) }

    fun updateData(newData: List<Cell>) {
        data = newData
        if (data.all { it.state == CellState.Painted && it.answer || it.state != CellState.Painted && !it.answer }) {
            cleared = true
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .onSizeChanged { screenWidth = it.width }
    ) {
        Display(data = data) {
            Field(
                data = data,
                startPaint = { row, col ->
                    if (touchMode) {
                        changingTo = CellState.nextForceState(data.cellState(row, col), paintMode)
                        updateData(data.stateForceUpdated(row, col, changingTo))
                    } else {
                        selectedRowIndex = row
                        selectedColumnIndex = col
                    }
                },
                paint = { row, col ->
                    if (touchMode) {

                        updateData(data.stateSuccessiveUpdated(row, col, changingTo, paintMode))
                    } else {
                        selectedRowIndex = row
                        selectedColumnIndex = col
                    }
                }
            )
            Selection(
                data = data,
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
                        val maxIndex = data.rowCount() - 1
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
                        updateData(
                            data.stateSuccessiveUpdated(
                                selectedRowIndex,
                                selectedColumnIndex,
                                changingTo,
                                paintMode
                            )
                        )
                    }
                },
                onPaintButtonPress = {
                    paintMode = CellState.Painted
                    changingTo =
                        CellState.nextForceState(data.cellState(selectedRowIndex, selectedColumnIndex), paintMode)
                    updateData(data.stateForceUpdated(selectedRowIndex, selectedColumnIndex, changingTo))
                    isSuccessive = true
                },
                onPaintButtonRemove = {
                    changingTo = CellState.Empty
                    isSuccessive = false
                },
                onCheckButtonPress = {
                    paintMode = CellState.Checked
                    changingTo =
                        CellState.nextForceState(data.cellState(selectedRowIndex, selectedColumnIndex), paintMode)
                    updateData(data.stateForceUpdated(selectedRowIndex, selectedColumnIndex, changingTo))
                    isSuccessive = true
                },
                onCheckButtonRemove = {
                    changingTo = CellState.Empty
                    isSuccessive = false
                }
            )
        }
        ClearDialog(
            cleared = cleared,
            onReset = {
                nextGame()
                data = originalData
                cleared = false
            }
        )
        DebugButtons(
            data = data,
            updateData = ::updateData,
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
            RowNumberSpace(data = data)
            ColumnNumbers(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { fieldSize = it.width },
                data = data
            )
        }
        Row(
            Modifier.fillMaxWidth()
        ) {
            RowNumbers(
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
fun ColumnNumbers(
    modifier: Modifier,
    data: List<Cell>
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        data.toColumnNumbers().forEach {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                it.forEach { number ->
                    HintNumber(number = number)
                }
            }
        }
    }
}

@Composable
fun RowNumbers(
    modifier: Modifier,
    data: List<Cell>
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        data.toRowNumbers().forEach {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                it.forEach { number ->
                    HintNumber(number = number)
                }
            }
        }
    }
}

@Composable
fun RowNumberSpace(
    data: List<Cell>
) {
    Row(
        Modifier.alpha(0f)
    ) {
        repeat(data.toRowNumbers().maxOf { it.size }) {
            HintNumber(number = 99)
        }
    }
}

@Composable
fun HintNumber(number: Int) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .width(IntrinsicSize.Max)
            .padding(horizontal = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
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