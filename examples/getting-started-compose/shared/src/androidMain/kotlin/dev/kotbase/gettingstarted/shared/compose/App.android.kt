package dev.kotbase.gettingstarted.shared.compose

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable

@Composable fun MainView() = App()

@Composable
actual fun BoxScope.Scrollbars(
    verticalScroll: ScrollState,
    horizontalScroll: ScrollState
) {
    // none
}
