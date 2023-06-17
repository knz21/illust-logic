package com.kenzo.logicbase

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

fun DrawScope.drawSquareStroke(
    color: Color,
    startOffset: Offset = Offset.Zero,
    size: Float,
    strokeWidth: Float
) {
    drawLine(
        color = color,
        start = startOffset,
        end = Offset(startOffset.x + size, startOffset.y),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = color,
        start = Offset(startOffset.x + size, startOffset.y),
        end = Offset(startOffset.x + size, startOffset.y + size),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = color,
        start = Offset(startOffset.x + size, startOffset.y + size),
        end = Offset(startOffset.x, startOffset.y + size),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = color,
        start = Offset(startOffset.x, startOffset.y + size),
        end = startOffset,
        strokeWidth = strokeWidth
    )
}