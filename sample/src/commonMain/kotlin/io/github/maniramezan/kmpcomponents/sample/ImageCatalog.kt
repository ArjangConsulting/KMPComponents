package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.maniramezan.kmpcomponents.SecondaryButton
import io.github.maniramezan.kmpcomponents.SectionCard
import io.github.maniramezan.kmpcomponents.image.CachedAsyncImage
import io.github.maniramezan.kmpcomponents.image.ImageCachePolicy
import io.github.maniramezan.kmpcomponents.kmpSpacing

/** CachedAsyncImage from the separate components-image artifact. */
@Composable
fun ImageSection() {
    var seed by remember { mutableIntStateOf(1) }
    val placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
    val error = ColorPainter(MaterialTheme.colorScheme.errorContainer)
    SectionCard(
        title = "Cached async image",
        description = "From io.github.maniramezan.kmpcomponents:components-image. The second image skips the " +
            "caches; the third has no data, so it shows the fallback.",
    ) {
        CachedAsyncImage(
            data = "$IMAGE_BASE_URL/$seed/600/300",
            contentDescription = "Sample photo $seed",
            modifier = Modifier.fillMaxWidth().height(IMAGE_HEIGHT).clip(MaterialTheme.shapes.medium),
            placeholder = placeholder,
            error = error,
            contentScale = ContentScale.Crop,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small)) {
            CachedAsyncImage(
                data = "$IMAGE_BASE_URL/${seed + 1}/200/200",
                contentDescription = "Uncached photo",
                modifier = Modifier.size(THUMBNAIL_SIZE).clip(MaterialTheme.shapes.small),
                memoryCachePolicy = ImageCachePolicy.DISABLED,
                diskCachePolicy = ImageCachePolicy.DISABLED,
                placeholder = placeholder,
                error = error,
                contentScale = ContentScale.Crop,
            )
            CachedAsyncImage(
                data = null,
                contentDescription = "Missing photo",
                modifier = Modifier.size(THUMBNAIL_SIZE).clip(MaterialTheme.shapes.small),
                fallback = placeholder,
            )
        }
        SecondaryButton(text = "Load another", onClick = { seed++ })
    }
}

private const val IMAGE_BASE_URL = "https://picsum.photos/seed"
private val IMAGE_HEIGHT = 180.dp
private val THUMBNAIL_SIZE = 96.dp
