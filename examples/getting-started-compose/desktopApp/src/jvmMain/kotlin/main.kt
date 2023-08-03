import androidx.compose.material.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.singleWindowApplication
import dev.kotbase.gettingstarted.shared.compose.MainView
import java.awt.Dimension

fun main() = singleWindowApplication(title = "Kotbase") {
    window.minimumSize = Dimension(400, 200)
    MaterialTheme {
        MainView()
    }
}
