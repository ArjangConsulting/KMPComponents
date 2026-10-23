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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.maniramezan.kmpcomponents.FormTextField
import io.github.maniramezan.kmpcomponents.SecondaryButton
import io.github.maniramezan.kmpcomponents.SectionCard
import io.github.maniramezan.kmpcomponents.image.CachedAsyncImage
import io.github.maniramezan.kmpcomponents.image.ImageCachePolicy
import io.github.maniramezan.kmpcomponents.kmpSpacing
import kotlin.io.encoding.Base64

/** CachedAsyncImage from the separate components-image artifact. */
@Composable
fun ImageSection() {
    var seed by remember { mutableIntStateOf(0) }
    var imageUrl by remember { mutableStateOf("") }
    var requestedUrl by remember { mutableStateOf<String?>(null) }
    val image = remember(seed) { Base64.decode(if (seed % 2 == 0) FIRST_IMAGE else SECOND_IMAGE) }
    val placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
    val error = ColorPainter(MaterialTheme.colorScheme.errorContainer)
    SectionCard(
        title = "Cached async image",
        description = "Generated illustrations load without a network connection. The thumbnail skips caches; " +
            "the empty image shows its fallback. Optionally enter a URL to try network loading.",
    ) {
        CachedAsyncImage(
            data = requestedUrl ?: image,
            contentDescription = "Example illustration",
            modifier = Modifier.fillMaxWidth().height(IMAGE_HEIGHT).clip(MaterialTheme.shapes.medium),
            placeholder = placeholder,
            error = error,
            contentScale = ContentScale.Crop,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small)) {
            CachedAsyncImage(
                data = image,
                contentDescription = "Uncached illustration",
                modifier = Modifier.size(THUMBNAIL_SIZE).clip(MaterialTheme.shapes.small),
                memoryCachePolicy = ImageCachePolicy.DISABLED,
                diskCachePolicy = ImageCachePolicy.DISABLED,
                placeholder = placeholder,
                error = error,
                contentScale = ContentScale.Crop,
            )
            CachedAsyncImage(
                data = null,
                contentDescription = "Missing image",
                modifier = Modifier.size(THUMBNAIL_SIZE).clip(MaterialTheme.shapes.small),
                fallback = placeholder,
            )
        }
        SecondaryButton(text = "Swap illustration", onClick = {
            requestedUrl = null
            seed++
        })
        FormTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = "Optional image URL",
            modifier = Modifier.fillMaxWidth(),
        )
        SecondaryButton(text = "Load URL", enabled = imageUrl.isNotBlank(), onClick = { requestedUrl = imageUrl })
    }
}

private const val FIRST_IMAGE =
    "iVBORw0KGgoAAAANSUhEUgAAACgAAAAYCAIAAAAH5iiXAAAAOUlEQVR4nGOQyW8iGz1dYUY2Yhi1eNTi" +
        "UYtHLR61mGiLU6aXk42eXd9ONhq1eNTiUYtHLR61mGgEACIKSvvANeolAAAAAElFTkSuQmCC"
private const val SECOND_IMAGE =
    "iVBORw0KGgoAAAANSUhEUgAAACgAAAAYCAIAAAAH5iiXAAAAOUlEQVR4nGN4usKMbJQyvZxsxDBq8ajF" +
        "oxaPWjxqMdEWP7u+nWwkk99ENhq1eNTiUYtHLR61mGgEAANwSvvfq1GgAAAAAElFTkSuQmCC"
private val IMAGE_HEIGHT = 180.dp
private val THUMBNAIL_SIZE = 96.dp
