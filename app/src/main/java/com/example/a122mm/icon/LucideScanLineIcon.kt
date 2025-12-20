package com.example.a122mm.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val LucideScanLine: ImageVector
    get() {
        if (_lucideScanLine != null) return _lucideScanLine!!
        _lucideScanLine = ImageVector.Builder(
            name = "LucideScanLine",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {

            path(
                stroke = SolidColor(Color.Black),
                fill = null,
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 7f)
                verticalLineTo(5f)
                quadTo(3f, 3f, 5f, 3f)
                horizontalLineTo(7f)
            }

            path(stroke = SolidColor(Color.Black), fill = null, strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(17f, 3f)
                horizontalLineTo(19f)
                quadTo(21f, 3f, 21f, 5f)
                verticalLineTo(7f)
            }

            path(stroke = SolidColor(Color.Black), fill = null, strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(21f, 17f)
                verticalLineTo(19f)
                quadTo(21f, 21f, 19f, 21f)
                horizontalLineTo(17f)
            }

            path(stroke = SolidColor(Color.Black), fill = null, strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(7f, 21f)
                horizontalLineTo(5f)
                quadTo(3f, 21f, 3f, 19f)
                verticalLineTo(17f)
            }

            path(
                stroke = SolidColor(Color.Black),
                fill = null,
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(7f, 12f)
                horizontalLineTo(17f)
            }
        }.build()
        return _lucideScanLine!!
    }

private var _lucideScanLine: ImageVector? = null
