package io.github.maniramezan.kmpcomponents.image

import coil3.request.CachePolicy
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageCachePolicyTest {
    @Test
    fun `every policy maps to the Coil policy with the same read and write rules`() {
        ImageCachePolicy.entries.forEach { policy ->
            val coil = policy.toCoilCachePolicy()
            assertEquals(policy.name, coil.name)
            assertEquals(policy == ImageCachePolicy.ENABLED || policy == ImageCachePolicy.READ_ONLY, coil.readEnabled)
            assertEquals(policy == ImageCachePolicy.ENABLED || policy == ImageCachePolicy.WRITE_ONLY, coil.writeEnabled)
        }
    }
}
