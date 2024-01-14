import androidx.compose.ui.window.singleWindowApplication
import java.awt.Dimension

fun main() = singleWindowApplication(title = "Kotbase") {
    window.minimumSize = Dimension(400, 200)
    App()
}
