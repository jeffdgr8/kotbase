import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import di.appModules
import ui.App
import ui.screen.NotesGrid
import domain.model.sampleNotes
import domain.replication.ReplicationService
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import java.awt.Dimension

fun main() = application {
    val windowState = rememberWindowState()
    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "Kotbase Notes"
    ) {
        window.minimumSize = Dimension(250, 250)

        KoinApplication(
            application = {
                modules(appModules)
            }
        ) {
            val replicatorService: ReplicationService = koinInject()
            LaunchedEffect(windowState.isMinimized, replicatorService) {
                with(replicatorService) {
                    if (windowState.isMinimized) stopReplication()
                    else startReplication()
                }
            }

            App()
        }
    }
}

@Preview
@Composable
fun AppDesktopPreview() {
    App()
}

@Preview
@Composable
fun NotesPreview() {
    NotesGrid(notes = sampleNotes, onNoteSelected = {})
}
