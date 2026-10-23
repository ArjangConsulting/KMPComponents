package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.material3.Card as MaterialCard

/** The optional body line of an [AssistantContextCard]; [isQuoted] shows it as an italic quote. */
@Immutable
public data class AssistantContextBody(public val text: String, public val isQuoted: Boolean = false)

/** The selected and enabled state of one [AssistantQuickActionChips] chip. */
@Immutable
public data class AssistantQuickActionState(public val isSelected: Boolean, public val isEnabled: Boolean)

/** The text of an [AssistantLimitPromptCard]. */
@Immutable
public data class AssistantLimitPromptCopy(
    public val message: String,
    public val supportingText: String,
    public val primaryActionLabel: String,
    public val secondaryActionLabel: String,
)

/**
 * What the assistant is looking at: a [title], an optional [highlight] such as the selected item,
 * an optional [body] line, and an optional [footnote]. Blank values are not drawn.
 */
@Composable
public fun AssistantContextCard(
    title: String,
    modifier: Modifier = Modifier,
    highlight: String? = null,
    body: AssistantContextBody? = null,
    footnote: String? = null,
) {
    MaterialCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.kmpSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.extraSmall),
        ) {
            Text(
                title,
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (!highlight.isNullOrBlank()) {
                Text(highlight, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }
            if (body != null && body.text.isNotBlank()) {
                Text(
                    text = if (body.isQuoted) "“${body.text}”" else body.text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = if (body.isQuoted) FontStyle.Italic else FontStyle.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!footnote.isNullOrBlank()) {
                Text(
                    footnote,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * A scrolling row of filter chips for assistant quick actions ("Summarize", "Explain"). The caller
 * owns the action type, labels, and selection and enablement rules. [icon] is decorative; the
 * chip's label and selected state carry its meaning.
 */
@Composable
public fun <T> AssistantQuickActionChips(
    actions: List<T>,
    actionState: (T) -> AssistantQuickActionState,
    label: @Composable (T) -> String,
    onAction: (T) -> Unit,
    modifier: Modifier = Modifier,
    icon: (T) -> Painter? = { null },
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
    ) {
        actions.forEach { action ->
            val state = actionState(action)
            val painter = icon(action)
            FilterChip(
                selected = state.isSelected,
                onClick = { onAction(action) },
                label = { Text(label(action)) },
                modifier = Modifier.heightIn(min = minimumTouchTargetSize()),
                enabled = state.isEnabled,
                leadingIcon = painter?.let { { Icon(it, contentDescription = null, Modifier.size(glyphSize())) } },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = colors.surface,
                    labelColor = colors.onSurfaceVariant,
                    selectedContainerColor = colors.primaryContainer,
                    selectedLabelColor = colors.onPrimaryContainer,
                    selectedLeadingIconColor = colors.onPrimaryContainer,
                ),
            )
        }
    }
}

/**
 * A full-width error banner for an unavailable provider, degraded service, or a blocking failure.
 * It is announced politely when it appears. For tones other than error, use [NoticeCard].
 */
@Composable
public fun AssistantStatusBanner(message: String, modifier: Modifier = Modifier) {
    MaterialCard(
        modifier = modifier.fillMaxWidth().semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
        ),
    ) {
        Text(message, Modifier.padding(MaterialTheme.kmpSpacing.medium), style = MaterialTheme.typography.bodyMedium)
    }
}

/** A card for usage limits, quotas, or upgrades, with a primary and a secondary action. */
@Composable
public fun AssistantLimitPromptCard(
    copy: AssistantLimitPromptCopy,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MaterialCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.kmpSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
        ) {
            Text(copy.message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = copy.supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PrimaryButton(text = copy.primaryActionLabel, onClick = onPrimaryAction, modifier = Modifier.fillMaxWidth())
            ThemeTextButton(
                text = copy.secondaryActionLabel,
                onClick = onSecondaryAction,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** A low-emphasis disclaimer or policy line under an assistant conversation. Blank draws nothing. */
@Composable
public fun AssistantDisclaimerFooter(text: String, modifier: Modifier = Modifier) {
    if (text.isBlank()) return
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.kmpSpacing.medium, vertical = MaterialTheme.kmpSpacing.small),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
