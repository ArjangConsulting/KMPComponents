package io.github.maniramezan.kmpcomponents

import androidx.compose.ui.platform.ClipEntry

/** A clipboard entry holding plain [text]; each platform wraps it in its native clip type. */
internal expect fun plainTextClipEntry(text: String): ClipEntry
