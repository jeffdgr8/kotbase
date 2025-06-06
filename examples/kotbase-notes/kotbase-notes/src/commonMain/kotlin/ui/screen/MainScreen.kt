package ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import domain.model.Note
import domain.model.sampleNotes
import kotlinx.coroutines.Dispatchers
import domain.randomNanoId
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import presentation.MainViewModel
import presentation.NotesState
import ui.widget.ReplicationStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onNoteSelected: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val viewModel: MainViewModel = koinInject { parametersOf(scope) }

    val notes by viewModel.notes.collectAsState()
    val searchText by viewModel.searchText.collectAsState(Dispatchers.Main.immediate)
    val useFts by viewModel.useFts.collectAsState(Dispatchers.Main.immediate)
    val replicationStatus by viewModel.replicationStatus.collectAsState()

    Scaffold(
        modifier = modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    ReplicationStatus(replicationStatus)
                    FtsSwitch(
                        ftsEnabled = useFts,
                        onFtsToggled = viewModel::updateUseFts
                    )
                    IconButton(
                        onClick = viewModel::logout
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onNoteSelected(randomNanoId())
                },
            ) {
                Icon(Icons.Filled.Add, "new note")
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding).padding(horizontal = 12.dp).padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SearchBar(
                text = searchText,
                onTextChange = viewModel::updateSearchText,
                modifier = Modifier.fillMaxWidth()
            )
            when (val state = notes) {
                NotesState.Loading -> LoadingIndicator()
                is NotesState.Notes -> NotesGrid(state.notes, onNoteSelected)
            }
        }
    }
}

@Composable
fun SearchBar(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = text,
        onValueChange = onTextChange,
        modifier = modifier,
        placeholder = {
            Text("Search notes")
        },
        leadingIcon = {
            Icon(Icons.Filled.Search, "search")
        },
        trailingIcon = {
            IconButton(
                onClick = { onTextChange("") },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Default)
            ) {
                Icon(Icons.Filled.Clear, "clear")
            }
        }
    )
}

@Composable
fun FtsSwitch(
    ftsEnabled: Boolean,
    onFtsToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.padding(horizontal = 8.dp)
    ) {
        Text("LIKE")
        Switch(
            checked = ftsEnabled,
            onCheckedChange = onFtsToggled
        )
        Text("FTS")
    }
}

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.weight(1F))
        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.weight(1F))
    }
}

@Composable
fun NotesGrid(
    notes: List<Note>,
    onNoteSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (notes.isNotEmpty()) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = 200.dp),
            verticalItemSpacing = 12.dp,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier
        ) {
            items(notes) { note ->
                NoteCard(note, onNoteSelected)
            }
        }
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(16.dp).fillMaxSize()
        ) {
            Text("Empty canvas. Let's start writing!")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCard(
    note: Note,
    onNoteSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = {
            onNoteSelected(note.id)
        },
        modifier = modifier
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(note.title, fontSize = 17.sp, fontWeight = FontWeight.Medium)
            Text(note.text, maxLines = 10, overflow = TextOverflow.Ellipsis)
            Text(note.edited, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Preview
@Composable
fun NotesPreview() {
    NotesGrid(notes = sampleNotes, onNoteSelected = {})
}
