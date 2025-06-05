package ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import presentation.AppViewModel
import presentation.Screen
import ui.screen.MainScreen
import ui.screen.EditScreen
import ui.screen.LoginScreen
import ui.screen.SplashScreen

@Composable
fun App(modifier: Modifier = Modifier) {
    MaterialTheme {
        val scope = rememberCoroutineScope()
        val viewModel: AppViewModel = koinInject { parametersOf(scope) }
        val screenState by viewModel.screen.collectAsState()

        AnimatedContent(
            targetState = screenState
        ) { screen ->
            when (screen) {
                is Screen.Splash -> SplashScreen()
                is Screen.Login -> LoginScreen()
                is Screen.Main -> MainScreen(
                    onNoteSelected = viewModel::selectNote
                )
                is Screen.Edit -> EditScreen(
                    noteId = screen.noteId,
                    onClose = viewModel::backPressed
                )
            }
        }

        BackHandler(viewModel.backHandlerEnabled) {
            viewModel.backPressed()
        }
    }
}

@Composable
expect fun BackHandler(isEnabled: Boolean = true, onBack: () -> Unit)
