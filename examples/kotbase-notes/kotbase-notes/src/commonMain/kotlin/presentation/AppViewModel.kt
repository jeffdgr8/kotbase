package presentation

import data.source.user.AuthStatus
import data.source.user.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppViewModel(
    scope: CoroutineScope,
    private val userRepository: UserRepository
) {

    private val _screen = MutableStateFlow<Screen>(Screen.Splash)
    val screen: StateFlow<Screen> get() = _screen

    init {
        scope.launch {
            userRepository.authStatus.collect {
                _screen.value = when (it) {
                    AuthStatus.LoggedIn -> Screen.Main
                    AuthStatus.LoggedOut -> Screen.Login
                    AuthStatus.Unknown -> Screen.Splash
                }
            }
        }
    }

    fun selectNote(id: String) {
        _screen.value = Screen.Edit(id)
    }

    fun returnToMain() {
        _screen.value = Screen.Main
    }

    val backHandlerEnabled: Boolean
        get() = screen.value != Screen.Main

    fun backPressed() {
        when (screen.value) {
            is Screen.Edit -> _screen.value = Screen.Main
            else -> {}
        }
    }
}
