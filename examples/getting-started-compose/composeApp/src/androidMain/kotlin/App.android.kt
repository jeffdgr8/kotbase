import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

@Composable
actual fun BoxScope.Scrollbars(
    verticalScroll: ScrollState,
    horizontalScroll: ScrollState
) {
    // none
}
