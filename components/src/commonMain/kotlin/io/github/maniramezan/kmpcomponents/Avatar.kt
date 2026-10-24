package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/** Diameter presets for [Avatar]. Each follows the user's font scale so initials never clip. */
public enum class AvatarSize {
    /** Twice the `labelMedium` line height (32 dp at 100% with the default tokens), for dense rows. */
    SMALL,

    /** Twice the `labelLarge` line height (40 dp by default), for list rows. */
    MEDIUM,

    /** Twice the `titleLarge` line height (56 dp by default), for headers and profile cards. */
    LARGE,
}

/**
 * A circular avatar for a person or entity: the caller's [image] when provided, otherwise the
 * initials derived from [name] on a tinted circle.
 *
 * Initials take the first letter of the first and last words of [name] (one letter for a single
 * word), skipping digits and punctuation. When [name] yields no letters and there is no [image], the
 * tinted circle renders empty.
 *
 * The avatar is one accessibility node with an image role, announced as [name]. Pass a blank [name]
 * with an [image] for a decorative avatar beside text that already names the person.
 *
 * @param name the display name, used for initials and the accessibility label.
 * @param size the avatar diameter.
 * @param image optional image content, for example a `CachedAsyncImage` with `ContentScale.Crop`.
 *   It is clipped to the circle and sized to fill it.
 */
@Composable
public fun Avatar(
    name: String,
    modifier: Modifier = Modifier,
    size: AvatarSize = AvatarSize.MEDIUM,
    image: (@Composable () -> Unit)? = null,
) {
    val initials = remember(name) { avatarInitials(name) }
    val style = size.initialsStyle()
    Box(
        modifier = modifier
            .size(glyphSize(style) * DIAMETER_LINE_HEIGHTS)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .clearAndSetSemantics {
                if (name.isNotBlank()) {
                    contentDescription = name
                    role = Role.Image
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        when {
            image != null -> Box(Modifier.matchParentSize(), propagateMinConstraints = true) { image() }

            initials.isNotEmpty() -> Text(
                text = initials,
                style = style,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

@Composable
@ReadOnlyComposable
private fun AvatarSize.initialsStyle(): TextStyle = when (this) {
    AvatarSize.SMALL -> MaterialTheme.typography.labelMedium
    AvatarSize.MEDIUM -> MaterialTheme.typography.labelLarge
    AvatarSize.LARGE -> MaterialTheme.typography.titleLarge
}

/**
 * Up to two uppercase initials from [name]: the first letter of the first and last words, where
 * words are separated by whitespace or punctuation and only letters count.
 */
internal fun avatarInitials(name: String): String {
    val letters = name.split(WORD_SEPARATOR).mapNotNull { word -> INITIAL.find(word)?.value }
    val first = letters.firstOrNull() ?: return ""
    val last = letters.drop(1).lastOrNull()
    return (first + last.orEmpty()).uppercase()
}

private const val DIAMETER_LINE_HEIGHTS = 2
private val WORD_SEPARATOR = Regex("[\\s\\p{Z}\\p{P}]+")
private val INITIAL = Regex("^\\p{L}\\p{M}*")
