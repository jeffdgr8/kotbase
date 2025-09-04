package presentation

import data.source.note.NoteRepository
import domain.model.Note
import domain.model.toLocalizedString
import domain.replication.ReplicationService
import kotbase.ReplicatorStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlin.time.Clock

class EditViewModel(
    scope: CoroutineScope,
    private val noteId: String,
    private val noteRepository: NoteRepository,
    private val replicationService: ReplicationService
) {

    val replicationStatus: StateFlow<ReplicatorStatus?>
        get() = replicationService.replicationStatus

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _note = MutableStateFlow(Note(edited = ""))
    val note: StateFlow<Note> get() = _note

    init {
        scope.launch {
            _note.value = noteRepository.getNote(noteId) ?: Note(id = noteId)
            _isLoading.value = false
        }
    }

    fun updateTitle(title: String) {
        updateNote {
            copy(title = title)
        }
    }

    fun updateText(text: String) {
        updateNote {
            copy(text = text)
        }
    }

    private fun updateNote(update: Note.() -> Note) {
        val timestamp = Clock.System.now()
        val updated = _note.updateAndGet {
            it.update().copy(
                modified = timestamp,
                edited = timestamp.toLocalizedString()
            )
        }
        noteRepository.save(updated)
    }

    fun deleteNote() {
        noteRepository.delete(note.value)
    }
}
