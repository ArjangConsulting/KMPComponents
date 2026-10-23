package io.github.maniramezan.kmpcomponents.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.NetworkClient
import coil3.network.NetworkFetcher
import coil3.network.NetworkRequest
import coil3.network.NetworkResponse
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import okio.IOException

/** Whether a [CachedAsyncImage] cache layer may be read, written, both, or neither. */
public enum class ImageCachePolicy {
    ENABLED,
    READ_ONLY,
    WRITE_ONLY,
    DISABLED,
}

/**
 * Loads an image from a URL, file, or bytes ([data]) with explicit memory, disk, and network cache
 * policies. Use it instead of depending on an image library from feature code; the loader (Coil)
 * is an implementation detail.
 *
 * [placeholder] shows while loading, [error] when loading fails, and [fallback] when [data] is
 * `null`. Pass `null` [contentDescription] only for decorative images.
 *
 * Network policies with reads disabled (`DISABLED` or `WRITE_ONLY`) never contact the server.
 * Memory and disk cache hits remain available according to their own policies; a cache miss shows
 * [error].
 *
 * @param memoryCacheKey a stable key for content whose URL changes (for example signed URLs);
 *   [diskCacheKey] defaults to it.
 */
@Composable
public fun CachedAsyncImage(
    data: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    memoryCachePolicy: ImageCachePolicy = ImageCachePolicy.ENABLED,
    diskCachePolicy: ImageCachePolicy = ImageCachePolicy.ENABLED,
    networkCachePolicy: ImageCachePolicy = ImageCachePolicy.ENABLED,
    memoryCacheKey: String? = null,
    diskCacheKey: String? = memoryCacheKey,
    crossfade: Boolean = true,
    placeholder: Painter? = null,
    error: Painter? = null,
    fallback: Painter? = error,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    clipToBounds: Boolean = true,
) {
    val context = LocalPlatformContext.current
    val request = remember(
        context,
        data,
        memoryCachePolicy,
        diskCachePolicy,
        networkCachePolicy,
        memoryCacheKey,
        diskCacheKey,
        crossfade,
    ) {
        ImageRequest.Builder(context)
            .data(data)
            .memoryCachePolicy(memoryCachePolicy.toCoilCachePolicy())
            .diskCachePolicy(diskCachePolicy.toCoilCachePolicy())
            .networkCachePolicy(networkCachePolicy.toCoilCachePolicy())
            .apply {
                if (!networkCachePolicy.toCoilCachePolicy().readEnabled) {
                    fetcherFactory(cacheOnlyNetworkFetcherFactory)
                }
            }
            .memoryCacheKey(memoryCacheKey)
            .diskCacheKey(diskCacheKey)
            .crossfade(crossfade)
            .build()
    }
    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        modifier = modifier,
        placeholder = placeholder,
        error = error,
        fallback = fallback,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        clipToBounds = clipToBounds,
    )
}

internal fun ImageCachePolicy.toCoilCachePolicy(): CachePolicy = when (this) {
    ImageCachePolicy.ENABLED -> CachePolicy.ENABLED
    ImageCachePolicy.READ_ONLY -> CachePolicy.READ_ONLY
    ImageCachePolicy.WRITE_ONLY -> CachePolicy.WRITE_ONLY
    ImageCachePolicy.DISABLED -> CachePolicy.DISABLED
}

@OptIn(ExperimentalCoilApi::class)
private val cacheOnlyNetworkFetcherFactory = NetworkFetcher.Factory(networkClient = { CacheOnlyNetworkClient })

private object CacheOnlyNetworkClient : NetworkClient {
    override suspend fun <T> executeRequest(
        request: NetworkRequest,
        block: suspend (NetworkResponse) -> T,
    ): T = throw IOException("Network reads are disabled")
}
