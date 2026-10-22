package io.github.maniramezan.kmpcomponents

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Semantic text roles for [AppText]. The first four are the kommon type scale; the rest re-cut a
 * Material slot at a heavier weight, keeping its size and line height.
 */
public enum class AppTextStyle {
    DISPLAY,
    TITLE,
    BODY,
    LABEL,
    LABEL_SMALL_SEMIBOLD,
    LABEL_SMALL_BOLD,
    LABEL_MEDIUM_SEMIBOLD,
    LABEL_MEDIUM_BOLD,
    LABEL_LARGE_BOLD,
    BODY_SMALL_MEDIUM,
    BODY_SMALL_SEMIBOLD,
    BODY_MEDIUM_SEMIBOLD,
    BODY_LARGE_SEMIBOLD,
}

/** Text in one of the theme's semantic [AppTextStyle] roles. */
@Composable
public fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    style: AppTextStyle = AppTextStyle.BODY,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(text = text, modifier = modifier, color = color, style = style.resolve(MaterialTheme.typography))
}

/** The [TextStyle] [this] role uses in [typography]. */
internal fun AppTextStyle.resolve(typography: Typography): TextStyle = when (this) {
    AppTextStyle.DISPLAY -> typography.displayLarge
    AppTextStyle.TITLE -> typography.titleLarge
    AppTextStyle.BODY -> typography.bodyLarge
    AppTextStyle.LABEL -> typography.labelMedium
    AppTextStyle.LABEL_SMALL_SEMIBOLD -> typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
    AppTextStyle.LABEL_SMALL_BOLD -> typography.labelSmall.copy(fontWeight = FontWeight.Bold)
    AppTextStyle.LABEL_MEDIUM_SEMIBOLD -> typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
    AppTextStyle.LABEL_MEDIUM_BOLD -> typography.labelMedium.copy(fontWeight = FontWeight.Bold)
    AppTextStyle.LABEL_LARGE_BOLD -> typography.labelLarge.copy(fontWeight = FontWeight.Bold)
    AppTextStyle.BODY_SMALL_MEDIUM -> typography.bodySmall.copy(fontWeight = FontWeight.Medium)
    AppTextStyle.BODY_SMALL_SEMIBOLD -> typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
    AppTextStyle.BODY_MEDIUM_SEMIBOLD -> typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
    AppTextStyle.BODY_LARGE_SEMIBOLD -> typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
}
