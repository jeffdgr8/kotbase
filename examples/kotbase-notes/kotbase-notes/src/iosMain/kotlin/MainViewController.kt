import androidx.compose.ui.window.ComposeUIViewController
import di.appModules
import org.koin.compose.KoinApplication
import ui.App

fun MainViewController() = ComposeUIViewController {
    KoinApplication(
        application = {
            modules(appModules)
        }
    ) {
        App()
    }
}
