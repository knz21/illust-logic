package com.kenzo.logicbase

import kotlin.random.Random

class LogicCreator(private val dataSize: Int) {

    companion object {

        private const val MinimumPaintableRate = 0.3f
    }

    fun create(): List<List<Boolean>> {
        var candidate: List<List<Boolean>>? = null
        while (candidate == null) {
            candidate = createAndTestify()
        }
        return candidate
    }

    private fun createAndTestify(): List<List<Boolean>>? {
        val candidate = createLogicData()
        val data = Cell.fromLogicData(candidate)
        val rowHints = data.toRowHints()
        val columnHints = data.toColumnHints()
        if (rowHints.paintableRate() < MinimumPaintableRate) {
            return null
        }
        if (columnHints.paintableRate() < MinimumPaintableRate) {
            return null
        }
        if (Resolver().resolve(rowHints, columnHints).size > 1) {
            return null
        }
        return candidate
    }

    private fun createLogicData(): List<List<Boolean>> = List(dataSize) { List(dataSize) { Random.nextInt() % 3 != 0 } }

    private fun List<List<Int>>.paintableRate(): Float = filter { it.firstPaintable() }.size / dataSize.toFloat()

    private fun List<Int>.firstPaintable(): Boolean {
        if (isEmpty()) return false
        val margin = dataSize - this.reduce { acc, i -> acc + i + 1 }
        return this.any { it > margin }
    }
}