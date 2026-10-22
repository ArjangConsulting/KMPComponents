package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.isSpecified

// Small line glyphs drawn on a Canvas so components need no icon-font dependency. They are
// decorative: callers put the accessible label on the interactive parent.

internal enum class Glyph { EYE, EYE_OFF, CLOSE, INFO }

/** A glyph sized to [style]'s line height, so it scales with the user's font size. */
@Composable
@ReadOnlyComposable
internal fun glyphSize(style: TextStyle = MaterialTheme.typography.bodyLarge): Dp = with(LocalDensity.current) {
    (if (style.lineHeight.isSpecified) style.lineHeight else style.fontSize).toDp()
}

private const val STROKE_FRACTION = 0.09f
private const val EYE_HEIGHT_FRACTION = 0.6f
private const val PUPIL_FRACTION = 0.25f
private const val CLOSE_INSET_FRACTION = 0.25f
private const val INFO_DOT_Y = 0.3f
private const val INFO_DOT_RADIUS = 0.75f
private const val INFO_STEM_TOP = 0.45f
private const val INFO_STEM_BOTTOM = 0.72f

@Composable
internal fun GlyphIcon(
    glyph: Glyph,
    modifier: Modifier = Modifier,
    size: Dp = glyphSize(),
    color: Color = LocalContentColor.current,
) {
    Canvas(modifier.size(size)) {
        val stroke = this.size.minDimension * STROKE_FRACTION
        val w = this.size.width
        val h = this.size.height
        when (glyph) {
            Glyph.EYE, Glyph.EYE_OFF -> {
                val eyeHeight = h * EYE_HEIGHT_FRACTION
                drawOval(color, Offset(0f, (h - eyeHeight) / 2f), Size(w, eyeHeight), style = Stroke(stroke))
                drawCircle(color, radius = eyeHeight * PUPIL_FRACTION, center = center)
                if (glyph == Glyph.EYE_OFF) {
                    drawLine(color, Offset(0f, 0f), Offset(w, h), strokeWidth = stroke, cap = StrokeCap.Round)
                }
            }

            Glyph.CLOSE -> {
                val inset = this.size.minDimension * CLOSE_INSET_FRACTION
                drawLine(color, Offset(inset, inset), Offset(w - inset, h - inset), stroke, StrokeCap.Round)
                drawLine(color, Offset(w - inset, inset), Offset(inset, h - inset), stroke, StrokeCap.Round)
            }

            Glyph.INFO -> {
                val radius = this.size.minDimension / 2f - stroke / 2f
                drawCircle(color, radius = radius, center = center, style = Stroke(stroke))
                drawCircle(color, radius = stroke * INFO_DOT_RADIUS, center = Offset(center.x, h * INFO_DOT_Y))
                drawLine(
                    color,
                    Offset(center.x, h * INFO_STEM_TOP),
                    Offset(center.x, h * INFO_STEM_BOTTOM),
                    stroke,
                    StrokeCap.Round,
                )
            }
        }
    }
}
