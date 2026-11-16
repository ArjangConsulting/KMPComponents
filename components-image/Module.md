# Module components-image

`CachedAsyncImage` loads an image from a URL, file, or bytes with explicit memory, disk, and
network cache policies. `ImageCachePolicy` says whether each cache layer may be read, written, both,
or neither. It is a separate artifact so that `components` stays free of an image loader.

Loading is backed by Coil 3 with a Ktor engine for each platform. Coil types stay out of the public
API, so apps do not depend on them directly. On the web there is no disk cache; the browser's HTTP
cache governs repeated loads.

# Package io.github.maniramezan.kmpcomponents.image

`CachedAsyncImage` and `ImageCachePolicy`.
