package io.github.maniramezan.kmpcomponents

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// A plus sign for tests and goldens; the library itself has no icon dependency.
internal val TestPlusIcon: ImageVector = ImageVector.Builder(
    name = "TestPlus",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).path(
    stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
    strokeLineWidth = 2f,
    strokeLineCap = StrokeCap.Round,
) {
    moveTo(12f, 5f)
    lineTo(12f, 19f)
    moveTo(5f, 12f)
    lineTo(19f, 12f)
}.build()
