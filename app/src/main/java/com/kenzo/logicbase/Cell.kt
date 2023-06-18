package com.kenzo.logicbase

import androidx.compose.runtime.Stable

@Stable
data class Cell(
    val rowIndex: Int,
    val colIndex: Int,
    val answer: Boolean,
    val state: CellState = CellState.Empty
) {

    companion object {

        fun fromLogicData(logicData: List<List<Boolean>>): List<Cell> = logicData.mapIndexed { rowIndex, row ->
            row.mapIndexed { colIndex, answer ->
                Cell(rowIndex, colIndex, answer = answer)
            }
        }.flatten()
    }
}

enum class CellState {
    Empty,
    Painted,
    Checked;

    companion object {

        fun nextForceState(current: CellState, nextCandidate: CellState): CellState =
            when (current) {
                nextCandidate -> Empty
                else -> nextCandidate
            }

        fun nextSuccessiveState(current: CellState, nextCandidate: CellState, paintMode: CellState): CellState =
            if (current == Empty) {
                nextCandidate
            } else if (nextCandidate == Empty && current == paintMode) {
                Empty
            } else {
                current
            }
    }
}

fun List<Cell>.columnCount(): Int = maxOf { it.colIndex } + 1

fun List<Cell>.rowCount(): Int = maxOf { it.rowIndex } + 1

fun List<Cell>.maxCount(): Int = maxOf(rowCount(), columnCount())

fun List<Cell>.cellState(row: Int, col: Int): CellState =
    find { it.rowIndex == row && it.colIndex == col }?.state ?: CellState.Empty

fun List<Cell>.stateForceUpdated(row: Int, col: Int, state: CellState): List<Cell> = map {
    if (it.rowIndex == row && it.colIndex == col) {
        it.copy(state = state)
    } else {
        it
    }
}

fun List<Cell>.stateSuccessiveUpdated(row: Int, col: Int, state: CellState, paintMode: CellState): List<Cell> = map {
    if (it.rowIndex == row && it.colIndex == col) {
        it.copy(state = CellState.nextSuccessiveState(it.state, state, paintMode))
    } else {
        it
    }
}

fun List<Cell>.answered(): List<Cell> = map {
    it.copy(state = if (it.answer) CellState.Painted else CellState.Checked)
}

fun List<Cell>.reset(): List<Cell> = map {
    it.copy(state = CellState.Empty)
}

fun List<Cell>.rows(index: Int): List<Cell> = filter { it.rowIndex == index }

fun List<Cell>.columns(index: Int): List<Cell> = filter { it.colIndex == index }

fun List<Cell>.toHints(): List<Int> {
    var hint = 0
    val hints = mutableListOf<Int>()
    forEach {
        if (it.answer) {
            hint++
        } else if (hint > 0) {
            hints.add(hint)
            hint = 0
        }
    }
    if (hint > 0) {
        hints.add(hint)
    }
    return hints
}

fun List<Cell>.toRowHints(): List<List<Int>> = (0 until rowCount()).map { row ->
    rows(row).toHints()
}

fun List<Cell>.toColumnHints(): List<List<Int>> = (0 until columnCount()).map { col ->
    columns(col).toHints()
}