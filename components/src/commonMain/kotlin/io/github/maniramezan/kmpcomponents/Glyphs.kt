package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.isSpecified

// Small line glyphs drawn on a Canvas so components need no icon-font dependency. They are
// decorative: callers put the accessible label on the interactive parent.

internal enum class Glyph { EYE, EYE_OFF, CLOSE, INFO, SEARCH, CHECK, CHEVRON_DOWN, COPY, SEND, MIC, STOP }

/** A glyph sized to [style]'s line height, so it scales with the user's font size. */
@Composable
@ReadOnlyComposable
internal fun glyphSize(style: TextStyle = MaterialTheme.typography.bodyLarge): Dp = with(LocalDensity.current) {
    (if (style.lineHeight.isSpecified) style.lineHeight else style.fontSize).toDp()
}

private const val STROKE_FRACTION = 0.09f
private const val EYE_CURVE_INSET = 0.22f
private const val EYE_CURVE_TOP = 0.08f
private const val PUPIL_FRACTION = 0.14f
private const val CLOSE_INSET_FRACTION = 0.25f
private const val INFO_DOT_Y = 0.3f
private const val INFO_DOT_RADIUS = 0.75f
private const val INFO_STEM_TOP = 0.45f
private const val INFO_STEM_BOTTOM = 0.72f
private const val SEARCH_LENS_CENTER = 0.42f
private const val SEARCH_LENS_RADIUS = 0.28f
private const val SEARCH_HANDLE_END = 0.86f
private const val HALF_SQRT_2 = 0.70710677f
private const val CHECK_START_X = 0.2f
private const val CHECK_START_Y = 0.52f
private const val CHECK_CORNER_X = 0.42f
private const val CHECK_CORNER_Y = 0.72f
private const val CHECK_END_X = 0.8f
private const val CHECK_END_Y = 0.3f
private const val CHEVRON_INSET = 0.25f
private const val CHEVRON_TOP = 0.38f
private const val CHEVRON_BOTTOM = 0.62f
private const val COPY_SHEET_INSET = 0.18f
private const val COPY_SHEET_SIZE = 0.52f
private const val COPY_OFFSET = 0.12f
private const val SEND_TOP = 0.2f
private const val SEND_BOTTOM = 0.8f
private const val SEND_HEAD_INSET = 0.26f
private const val SEND_HEAD_DROP = 0.44f
private const val MIC_WIDTH = 0.32f
private const val MIC_TOP = 0.12f
private const val MIC_HEIGHT = 0.46f
private const val MIC_CUP_INSET = 0.22f
private const val MIC_CUP_TOP = 0.36f
private const val MIC_CUP_BOTTOM = 0.72f
private const val MIC_BASE = 0.88f
private const val STOP_INSET = 0.28f

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
                val inset = stroke / 2f
                val eyeOutline = Path().apply {
                    moveTo(inset, center.y)
                    cubicTo(
                        w * EYE_CURVE_INSET,
                        h * EYE_CURVE_TOP,
                        w * (1f - EYE_CURVE_INSET),
                        h * EYE_CURVE_TOP,
                        w - inset,
                        center.y,
                    )
                    cubicTo(
                        w * (1f - EYE_CURVE_INSET),
                        h * (1f - EYE_CURVE_TOP),
                        w * EYE_CURVE_INSET,
                        h * (1f - EYE_CURVE_TOP),
                        inset,
                        center.y,
                    )
                    close()
                }
                drawPath(eyeOutline, color, style = Stroke(stroke, join = StrokeJoin.Round))
                drawCircle(
                    color,
                    radius = this.size.minDimension * PUPIL_FRACTION,
                    center = center,
                    style = Stroke(stroke),
                )
                if (glyph == Glyph.EYE_OFF) {
                    drawLine(
                        color,
                        Offset(stroke, stroke),
                        Offset(w - stroke, h - stroke),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round,
                    )
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

            Glyph.SEARCH -> {
                val lensCenter = Offset(w * SEARCH_LENS_CENTER, h * SEARCH_LENS_CENTER)
                val lensRadius = this.size.minDimension * SEARCH_LENS_RADIUS
                drawCircle(color, radius = lensRadius, center = lensCenter, style = Stroke(stroke))
                // The handle starts on the lens rim at 45 degrees and runs to the bottom-end corner.
                val rim = lensRadius * HALF_SQRT_2
                drawLine(
                    color,
                    Offset(lensCenter.x + rim, lensCenter.y + rim),
                    Offset(w * SEARCH_HANDLE_END, h * SEARCH_HANDLE_END),
                    stroke,
                    StrokeCap.Round,
                )
            }

            Glyph.CHECK -> {
                val corner = Offset(w * CHECK_CORNER_X, h * CHECK_CORNER_Y)
                drawLine(color, Offset(w * CHECK_START_X, h * CHECK_START_Y), corner, stroke, StrokeCap.Round)
                drawLine(color, corner, Offset(w * CHECK_END_X, h * CHECK_END_Y), stroke, StrokeCap.Round)
            }

            Glyph.COPY -> {
                // Two overlapping sheets: the back one offset up and to the start.
                val sheet = Size(w * COPY_SHEET_SIZE, h * COPY_SHEET_SIZE)
                val corner = CornerRadius(stroke, stroke)
                val front = Offset(w * (COPY_SHEET_INSET + COPY_OFFSET), h * (COPY_SHEET_INSET + COPY_OFFSET))
                val back = Offset(w * COPY_SHEET_INSET, h * COPY_SHEET_INSET)
                drawRoundRect(color, topLeft = back, size = sheet, cornerRadius = corner, style = Stroke(stroke))
                drawRoundRect(color, topLeft = front, size = sheet, cornerRadius = corner, style = Stroke(stroke))
            }

            Glyph.SEND -> {
                // An upward arrow: the direction a sent message moves, the same in LTR and RTL.
                val tip = Offset(center.x, h * SEND_TOP)
                drawLine(color, tip, Offset(center.x, h * SEND_BOTTOM), stroke, StrokeCap.Round)
                drawLine(color, Offset(w * SEND_HEAD_INSET, h * SEND_HEAD_DROP), tip, stroke, StrokeCap.Round)
                drawLine(color, tip, Offset(w * (1f - SEND_HEAD_INSET), h * SEND_HEAD_DROP), stroke, StrokeCap.Round)
            }

            Glyph.MIC -> {
                val capsuleWidth = w * MIC_WIDTH
                drawRoundRect(
                    color,
                    topLeft = Offset(center.x - capsuleWidth / 2f, h * MIC_TOP),
                    size = Size(capsuleWidth, h * MIC_HEIGHT),
                    cornerRadius = CornerRadius(capsuleWidth / 2f, capsuleWidth / 2f),
                )
                val cup = Rect(
                    w * MIC_CUP_INSET,
                    h * (2 * MIC_CUP_TOP - MIC_CUP_BOTTOM),
                    w * (1f - MIC_CUP_INSET),
                    h * MIC_CUP_BOTTOM,
                )
                drawArc(
                    color,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = cup.topLeft,
                    size = cup.size,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
                drawLine(
                    color,
                    Offset(center.x, h * MIC_CUP_BOTTOM),
                    Offset(center.x, h * MIC_BASE),
                    stroke,
                    StrokeCap.Round,
                )
            }

            Glyph.STOP -> {
                val side = this.size.minDimension * (1f - 2 * STOP_INSET)
                drawRoundRect(
                    color,
                    topLeft = Offset(center.x - side / 2f, center.y - side / 2f),
                    size = Size(side, side),
                    cornerRadius = CornerRadius(stroke, stroke),
                )
            }

            Glyph.CHEVRON_DOWN -> {
                val tip = Offset(center.x, h * CHEVRON_BOTTOM)
                drawLine(color, Offset(w * CHEVRON_INSET, h * CHEVRON_TOP), tip, stroke, StrokeCap.Round)
                drawLine(color, tip, Offset(w * (1f - CHEVRON_INSET), h * CHEVRON_TOP), stroke, StrokeCap.Round)
            }
        }
    }
}
