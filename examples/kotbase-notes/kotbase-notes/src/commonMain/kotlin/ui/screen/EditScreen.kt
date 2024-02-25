package ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import presentation.EditViewModel
import ui.tabFocus
import ui.widget.ReplicationStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    noteId: String,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val viewModel: EditViewModel = koinInject { parametersOf(scope, noteId) }

    val isLoading by viewModel.isLoading.collectAsState()
    val note by viewModel.note.collectAsState(Dispatchers.Main.immediate)
    val replicationStatus by viewModel.replicationStatus.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.ArrowBack, "back")
                    }
                },
                actions = {
                    ReplicationStatus(replicationStatus)
                    IconButton(
                        onClick = {
                            viewModel.deleteNote()
                            onClose()
                        }
                    ) {
                        Icon(Icons.Filled.Delete, "delete note")
                    }
                }
            )
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier.padding(contentPadding).padding(horizontal = 12.dp).padding(bottom = 12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Title(note.title, viewModel::updateTitle, modifier = Modifier.fillMaxWidth())
                NoteText(note.text, viewModel::updateText, modifier = Modifier.weight(1F).fillMaxWidth())
                Text("Edited ${note.edited}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun Title(
    title: String,
    onTitleChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        modifier = modifier.onPreviewKeyEvent(tabFocus(focusManager, FocusDirection.Down)),
        placeholder = { Text("Title") },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
    )
}

@Composable
fun NoteText(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        modifier = modifier.onPreviewKeyEvent(tabFocus(focusManager, FocusDirection.Up)),
        placeholder = { Text("Note") },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
    )
}
