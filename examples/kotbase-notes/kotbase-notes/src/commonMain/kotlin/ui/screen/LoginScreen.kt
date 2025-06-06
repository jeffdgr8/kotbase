package ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import presentation.LoginViewModel
import ui.handleEnter
import ui.tabFocus

@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val viewModel: LoginViewModel = koinInject { parametersOf(scope) }

    val username by viewModel.username.collectAsState(Dispatchers.Main.immediate)
    val password by viewModel.password.collectAsState(Dispatchers.Main.immediate)
    val isLoginButtonEnabled by viewModel.isLoginButtonEnabled.collectAsState(Dispatchers.Main.immediate)

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.messages
            .flowOn(Dispatchers.Main.immediate)
            .collect { message ->
                message?.let { snackbarHostState.showSnackbar(it) }
            }
    }

    Scaffold(
        modifier = modifier.imePadding(),
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { contentPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(contentPadding).fillMaxWidth().padding(12.dp)
        ) {
            Spacer(modifier = Modifier.weight(1F))

            Text("Kotbase Notes", fontSize = 32.sp, color = Color.Red)

            Spacer(modifier = Modifier.weight(0.3F))

            UsernameField(
                value = username,
                onValueChange = viewModel::updateUsername,
                onEnterPressed = viewModel::login
            )

            PasswordField(
                value = password,
                onValueChange = viewModel::updatePassword,
                onEnterPressed = viewModel::login
            )

            Button(
                onClick = viewModel::login,
                enabled = isLoginButtonEnabled
            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.weight(1F))
        }
    }
}

@Composable
fun UsernameField(
    value: String,
    onValueChange: (String) -> Unit,
    onEnterPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Username") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        modifier = modifier.onPreviewKeyEvent(tabFocus(focusManager, FocusDirection.Next))
            .onPreviewKeyEvent(handleEnter(onEnterPressed))
    )
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    onEnterPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPassword by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Password") },
        singleLine = true,
        trailingIcon = {
            IconButton(
                onClick = { showPassword = !showPassword },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Default)
            ) {
                Icon(if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, "show password")
            }
        },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Go),
        keyboardActions = KeyboardActions(onGo = { onEnterPressed() }),
        modifier = modifier.onPreviewKeyEvent(tabFocus(focusManager, FocusDirection.Next))
            .onPreviewKeyEvent(handleEnter(onEnterPressed))
    )
}
