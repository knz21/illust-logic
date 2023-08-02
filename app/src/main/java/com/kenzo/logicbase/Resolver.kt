package com.kenzo.logicbase

class Resolver {

    fun resolve(rowHints: List<List<Int>>, colHints: List<List<Int>>): Set<List<List<Boolean>>> {
        val count: Int = rowHints.size

        val rowPatterns = rowHints.map { numbers ->
            generatePaintedPatterns(count, numbers)
        }
        val patternsFromRowNumbers = patterns(rowPatterns)

        val colPatterns = colHints.map { numbers ->
            generatePaintedPatterns(count, numbers)
        }
        val patternsFromColNumbers = patterns(colPatterns).map {
            transposeLists(it)
        }

        return patternsFromColNumbers.intersect(patternsFromRowNumbers.toSet())
    }
}

fun generatePaintedPatterns(size: Int, numbers: List<Int>): List<List<Boolean>> {
    val patterns = mutableListOf<List<Boolean>>()
    val currentPattern = BooleanArray(size) // 現在のパターンを表すBoolean配列
    generatePatterns(size, numbers, 0, 0, currentPattern, patterns)
    return patterns
}

fun generatePatterns(
    size: Int,
    numbers: List<Int>,
    startIndex: Int,
    currentIndex: Int,
    currentPattern: BooleanArray,
    patterns: MutableList<List<Boolean>>
) {
    if (currentIndex == numbers.size) {
        patterns.add(currentPattern.toList())
        return
    }

    for (i in startIndex until size) {
        // 現在の数値で指定されたマスを塗る
        val currentValue = numbers[currentIndex]
        if (i + currentValue <= size) {
            for (j in i until i + currentValue) {
                currentPattern[j] = true
            }

            // 次の数値に進む
            generatePatterns(size, numbers, i + currentValue + 1, currentIndex + 1, currentPattern, patterns)

            // 塗ったマスを元に戻す
            for (j in i until i + currentValue) {
                currentPattern[j] = false
            }
        }
    }
}



fun patterns(positions: List<List<List<Boolean>>>): List<List<List<Boolean>>> {
    val patterns = mutableListOf<List<List<Boolean>>>()

    generatePattern(positions, 0, mutableListOf(), patterns)

    return patterns
}

fun generatePattern(
    positions: List<List<List<Boolean>>>,
    currentPosition: Int,
    currentPattern: MutableList<List<Boolean>>,
    patterns: MutableList<List<List<Boolean>>>
) {
    if (currentPosition >= positions.size) {
        patterns.add(currentPattern.toList())
        return
    }

    val currentCandidates = positions[currentPosition]

    for (candidate in currentCandidates) {
        currentPattern.add(candidate)
        generatePattern(positions, currentPosition + 1, currentPattern, patterns)
        currentPattern.removeAt(currentPattern.size - 1)
    }
}

fun transposeLists(lists: List<List<Boolean>>): List<List<Boolean>> {
    val numRows = lists.size
    val numCols = lists[0].size

    val transposedLists = MutableList(numCols) { MutableList(numRows) { false } }

    for (row in 0 until numRows) {
        for (col in 0 until numCols) {
            transposedLists[col][row] = lists[row][col]
        }
    }

    return transposedLists
}