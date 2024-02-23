package presentation

import domain.replication.AuthService
import domain.replication.AuthStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AppViewModel(
    scope: CoroutineScope,
    private val authService: AuthService
) {

    private val _screen = MutableStateFlow<Screen>(Screen.Splash)
    val screen: StateFlow<Screen> get() = _screen

    init {
        authService.authStatus
            .onEach {
                _screen.value = when (it) {
                    AuthStatus.LoggedIn -> Screen.Main
                    AuthStatus.LoggedOut -> Screen.Login
                    AuthStatus.Unknown -> Screen.Splash
                }
            }
            .launchIn(scope)
    }

    fun selectNote(id: String) {
        _screen.value = Screen.Edit(id)
    }

    val backHandlerEnabled: Boolean
        get() = screen.value is Screen.Edit

    fun backPressed() {
        when (screen.value) {
            is Screen.Edit -> _screen.value = Screen.Main
            else -> {}
        }
    }
}
