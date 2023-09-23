package com.kenzo.logicbase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kenzo.logicbase.ui.theme.LogicBaseTheme

private const val DataSize = 5

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LogicBaseTheme {
                val logicCreator = remember { LogicCreator(DataSize) }
                var cells by remember { mutableStateOf(Cell.fromLogicData(logicCreator.create())) }
                GameScreen(
                    cells = cells,
                    nextGame = { cells = Cell.fromLogicData(logicCreator.create()) },
                    updateData = { cells = it },
                    forceUpdateData = { row, col, paintMode ->
                        val state = CellState.nextForceState(cells.cellState(row, col), paintMode)
                        cells = cells.stateForceUpdated(row, col, state)
                        state
                    },
                    successiveUpdateData = { row, col, changingTo, paintMode ->
                        cells = cells.stateSuccessiveUpdated(row, col, changingTo, paintMode)
                    }
                )
            }
        }
    }
}