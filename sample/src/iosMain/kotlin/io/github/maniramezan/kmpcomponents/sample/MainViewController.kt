package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** The catalog as a UIKit view controller, for the iOS host app in `sample/ios`. */
fun mainViewController(): UIViewController = ComposeUIViewController { ComponentCatalog() }
