package presentation

import domain.replication.AuthService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val scope: CoroutineScope,
    private val authService: AuthService
) {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> get() = _username

    fun updateUsername(username: String) {
        _username.value = username
        checkEnableLoginButton()
    }

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> get() = _password

    fun updatePassword(password: String) {
        _password.value = password
        checkEnableLoginButton()
    }

    private fun checkEnableLoginButton() {
        _isLoginButtonEnabled.value = username.value.isNotBlank() && password.value.isNotBlank()
    }

    private val _isLoginButtonEnabled = MutableStateFlow(false)
    val isLoginButtonEnabled: StateFlow<Boolean> get() = _isLoginButtonEnabled

    private val messageChannel = Channel<String?>(Channel.BUFFERED)
    val messages = messageChannel.receiveAsFlow()

    fun login() {
        if (!isLoginButtonEnabled.value) return
        _isLoginButtonEnabled.value = false
        scope.launch {
            if (!authService.authenticateUser(username.value, password.value)) {
                _isLoginButtonEnabled.value = true
                messageChannel.send("Invalid credentials")
            }
        }
    }
}
