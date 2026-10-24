package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction

/** Localizable labels for [SearchField]. */
@Immutable
public data class SearchFieldLabels(
    /** Placeholder shown while the field is empty; blank shows none. */
    public val placeholder: String = "Search",
    /** Accessible name and tooltip of the clear button. */
    public val clear: String = "Clear search",
)

/**
 * A single-line search field with a search glyph and a clear button that appears once the field
 * has text. The placeholder also supplies the accessible name after typing. Clearing calls
 * [onValueChange] with an empty string.
 *
 * The keyboard shows a Search action; [onSearch] runs when it is pressed. For filter-as-you-type
 * lists, leave [onSearch] `null` and react to [onValueChange].
 */
@Composable
public fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSearch: ((String) -> Unit)? = null,
    labels: SearchFieldLabels = SearchFieldLabels(),
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.semantics {
            if (labels.placeholder.isNotBlank()) contentDescription = labels.placeholder
        },
        enabled = enabled,
        singleLine = true,
        placeholder = labels.placeholder.takeIf { it.isNotBlank() }?.let { text -> { Text(text) } },
        leadingIcon = {
            GlyphIcon(glyph = Glyph.SEARCH, color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingIcon = if (value.isNotEmpty()) {
            {
                GlyphIconButton(
                    glyph = Glyph.CLOSE,
                    label = labels.clear,
                    onClick = { onValueChange("") },
                    enabled = enabled,
                )
            }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = if (onSearch != null) {
            KeyboardActions(onSearch = { onSearch(value) })
        } else {
            KeyboardActions.Default
        },
    )
}
