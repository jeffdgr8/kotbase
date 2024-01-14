import App
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun AppPreview() {
    App()
}

@Composable
actual fun BoxScope.Scrollbars(
    verticalScroll: ScrollState,
    horizontalScroll: ScrollState
) {
    VerticalScrollbar(
        modifier = Modifier.align(Alignment.CenterEnd)
            .fillMaxHeight()
            .padding(bottom = 8.dp),
        adapter = rememberScrollbarAdapter(verticalScroll)
    )
    HorizontalScrollbar(
        modifier = Modifier.align(Alignment.BottomStart)
            .fillMaxWidth()
            .padding(end = 8.dp),
        adapter = rememberScrollbarAdapter(horizontalScroll)
    )
}
