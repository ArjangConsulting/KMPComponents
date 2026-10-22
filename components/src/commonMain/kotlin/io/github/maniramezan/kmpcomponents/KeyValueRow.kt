package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign

/**
 * A read-only label/value pair such as "Model · llama3" or "Port · 8080", for details panels and
 * popovers. The value is end-aligned and wraps; screen readers read the pair as one item.
 *
 * @param valueStyle the value's text style; `null` uses `typography.bodyMedium`. Pass
 *   `MaterialTheme.kmpTypography.code` for identifiers, paths, and arguments.
 * @param trailingContent optional control after the value, such as a copy button.
 */
@Composable
public fun KeyValueRow(
    key: String,
    value: String,
    modifier: Modifier = Modifier,
    valueStyle: TextStyle? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f).semantics(mergeDescendants = true) {},
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.medium),
        ) {
            Text(key, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = value,
                modifier = Modifier.weight(1f),
                style = valueStyle ?: MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
            )
        }
        trailingContent?.invoke()
    }
}
