package io.github.maniramezan.kmpcomponents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.stateDescription
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Localizable labels for [CopyButton]. */
@Immutable
public data class CopyButtonLabels(
    /** Accessible name and tooltip. */
    public val copy: String = "Copy",
    /** State announced after copying. */
    public val copied: String = "Copied",
)

/**
 * An icon button that copies [text] to the system clipboard as plain text. After copying it shows a
 * check mark for a moment and announces [CopyButtonLabels.copied]. [onCopied] runs after the copy,
 * for example to log it.
 */
@Composable
public fun CopyButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    labels: CopyButtonLabels = CopyButtonLabels(),
    onCopied: (() -> Unit)? = null,
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(COPIED_FEEDBACK_MILLIS)
            copied = false
        }
    }
    val iconSize = glyphSize()
    TooltipIconButton(
        label = labels.copy,
        onClick = {
            scope.launch {
                clipboard.setClipEntry(plainTextClipEntry(text))
                copied = true
                onCopied?.invoke()
            }
        },
        iconSize = iconSize,
        modifier = modifier,
        enabled = enabled,
        extraSemantics = {
            if (copied) {
                stateDescription = labels.copied
                liveRegion = LiveRegionMode.Polite
            }
        },
    ) {
        GlyphIcon(glyph = if (copied) Glyph.CHECK else Glyph.COPY, size = iconSize)
    }
}

private const val COPIED_FEEDBACK_MILLIS = 1_500L
