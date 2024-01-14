import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { App() }

@Composable
actual fun BoxScope.Scrollbars(
    verticalScroll: ScrollState,
    horizontalScroll: ScrollState
) {
    // none
}
