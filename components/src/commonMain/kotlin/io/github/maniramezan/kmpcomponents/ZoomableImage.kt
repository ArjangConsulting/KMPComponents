package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.DpSize
import kotlin.math.roundToInt

/** Localizable labels for [ZoomableImage]. */
@Immutable
public data class ZoomableImageLabels(
    /** The spoken zoom level, such as "Zoom 200%". */
    public val zoomDescription: (zoom: Float) -> String = { "Zoom ${(it * PERCENT).roundToInt()}%" },
)

/**
 * Shows [image] fitted to the space, for screenshots and previews. Zoom with the mouse wheel or the
 * plus and minus keys when focused, drag to pan while zoomed, and double-click or press 0 to reset.
 * [marker], in image pixels, draws a ring where something happened, such as a tap. The zoom level is
 * internal UI state; it resets when the image size changes while not zoomed. [maxZoom] must be
 * finite and at least 1; lowering it clamps the current zoom.
 */
@Composable
public fun ZoomableImage(
    image: ImageBitmap,
    contentDescription: String,
    modifier: Modifier = Modifier,
    marker: Offset? = null,
    maxZoom: Float = DEFAULT_MAX_ZOOM,
    labels: ZoomableImageLabels = ZoomableImageLabels(),
) {
    require(maxZoom.isFinite() && maxZoom >= 1f) { "maxZoom must be finite and at least 1" }
    val currentMaxZoom by rememberUpdatedState(maxZoom)
    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    LaunchedEffect(image.width, image.height) { if (zoom == 1f) pan = Offset.Zero }
    fun zoomTo(next: Float) {
        zoom = next.coerceIn(1f, currentMaxZoom)
        if (zoom == 1f) pan = Offset.Zero
    }
    LaunchedEffect(maxZoom) { zoomTo(zoom) }
    val markerColor = MaterialTheme.colorScheme.primary
    val markerSize = glyphSize()
    BoxWithConstraints(
        modifier = modifier
            .keyboardFocusOutline(MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .pointerInput(maxZoom) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Scroll) {
                            val delta = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
                            zoomTo(zoomForScroll(zoom, delta))
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, drag ->
                    if (zoom > 1f) {
                        change.consume()
                        pan += drag
                    }
                }
            }
            .pointerInput(Unit) { detectTapGestures(onDoubleTap = { zoomTo(1f) }) }
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    Key.Plus, Key.Equals, Key.NumPadAdd -> zoomTo(zoom * ZOOM_STEP)
                    Key.Minus, Key.NumPadSubtract -> zoomTo(zoom / ZOOM_STEP)
                    Key.Zero, Key.NumPad0 -> zoomTo(1f)
                    else -> return@onKeyEvent false
                }
                true
            }
            .semantics { stateDescription = labels.zoomDescription(zoom) }
            .focusable(),
        contentAlignment = Alignment.Center,
    ) {
        val widthScale = if (constraints.hasBoundedWidth) {
            constraints.maxWidth.toFloat() / image.width
        } else {
            Float.POSITIVE_INFINITY
        }
        val heightScale = if (constraints.hasBoundedHeight) {
            constraints.maxHeight.toFloat() / image.height
        } else {
            Float.POSITIVE_INFINITY
        }
        val fit = minOf(widthScale, heightScale).takeIf { it.isFinite() } ?: 1f
        val shown = with(LocalDensity.current) { DpSize((image.width * fit).toDp(), (image.height * fit).toDp()) }
        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = zoom
                scaleY = zoom
                translationX = pan.x
                translationY = pan.y
            },
            contentAlignment = Alignment.Center,
        ) {
            Image(image, contentDescription, Modifier.size(shown), contentScale = ContentScale.Fit)
            if (marker != null) {
                Canvas(Modifier.matchParentSize()) {
                    val center = Offset(marker.x * fit, marker.y * fit)
                    val radius = markerSize.toPx() / 2f
                    drawCircle(
                        markerColor,
                        radius = radius,
                        center = center,
                        style = Stroke(radius * MARKER_STROKE_FRACTION),
                    )
                    drawCircle(markerColor.copy(alpha = MARKER_FILL_ALPHA), radius = radius / 2f, center = center)
                }
            }
        }
    }
}

/** The zoom after one wheel notch: scrolling up ([scrollDeltaY] below 0) zooms in by one step. */
internal fun zoomForScroll(current: Float, scrollDeltaY: Float): Float = when {
    scrollDeltaY < 0f -> current * ZOOM_STEP
    scrollDeltaY > 0f -> current / ZOOM_STEP
    else -> current
}

private const val PERCENT = 100
private const val ZOOM_STEP = 1.1f
private const val DEFAULT_MAX_ZOOM = 6f
private const val MARKER_STROKE_FRACTION = 0.2f
private const val MARKER_FILL_ALPHA = 0.35f
