package ru.jone501.myfefu.ui.lucide

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("FunctionName")
fun EyeClosed(color: Color = Color.Black, strokeWidth: Float = 2f): ImageVector {
    return ImageVector.Builder(
        name = "eye-closed",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(color),
            strokeLineWidth = strokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(15f, 18f)
            lineToRelative(-0.722f, -3.25f)
        }
        path(
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(color),
            strokeLineWidth = strokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(2f, 8f)
            arcToRelative(10.645f, 10.645f, 0f, false, false, 20f, 0f)
        }
        path(
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(color),
            strokeLineWidth = strokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(20f, 15f)
            lineToRelative(-1.726f, -2.05f)
        }
        path(
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(color),
            strokeLineWidth = strokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(4f, 15f)
            lineToRelative(1.726f, -2.05f)
        }
        path(
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(color),
            strokeLineWidth = strokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(9f, 18f)
            lineToRelative(0.722f, -3.25f)
        }
    }.build()
}