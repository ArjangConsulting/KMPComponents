package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// Line icons for the catalog. The library takes caller-supplied painters, so apps can use any icon
// set; the sample draws its own to stay dependency-free.

val PlusIcon: ImageVector = lineIcon("Plus") {
    moveTo(12f, 5f)
    lineTo(12f, 19f)
    moveTo(5f, 12f)
    lineTo(19f, 12f)
}

val RefreshIcon: ImageVector = lineIcon("Refresh") {
    moveTo(19f, 12f)
    arcTo(7f, 7f, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = 16.95f, y1 = 7.05f)
    moveTo(17f, 3f)
    lineTo(17f, 7f)
    lineTo(13f, 7f)
}

private fun lineIcon(name: String, block: PathBuilder.() -> Unit): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).path(
    stroke = SolidColor(Color.Black),
    strokeLineWidth = 2f,
    strokeLineCap = StrokeCap.Round,
    strokeLineJoin = StrokeJoin.Round,
    pathBuilder = block,
).build()
