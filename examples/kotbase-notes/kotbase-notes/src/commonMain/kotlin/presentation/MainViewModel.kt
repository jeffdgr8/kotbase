package presentation

import data.source.note.NoteRepository
import domain.model.Note
import domain.replication.AuthService
import domain.replication.ReplicationService
import kotbase.ReplicatorStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class MainViewModel(
    scope: CoroutineScope,
    private val noteRepository: NoteRepository,
    private val authService: AuthService,
    private val replicationService: ReplicationService
) {

    val replicationStatus: StateFlow<ReplicatorStatus?>
        get() = replicationService.replicationStatus

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> get() = _searchText

    fun updateSearchText(text: String) {
        _searchText.value = text
    }

    private val _useFts = MutableStateFlow(false)
    val useFts: StateFlow<Boolean> get() = _useFts

    fun updateUseFts(useFts: Boolean) {
        _useFts.value = useFts
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<NotesState> =
        combine(searchText, useFts) { searchText, useFts ->
            Pair(searchText, useFts)
        }
            .flatMapLatest { (searchText, useFts) ->
                noteRepository.getNotesFlow(searchText, useFts)
            }
            .map { NotesState.Notes(it) }
            .stateIn(scope, SharingStarted.Eagerly, NotesState.Loading)

    fun logout() {
        authService.logout()
    }
}

sealed interface NotesState {
    data object Loading : NotesState
    data class Notes(val notes: List<Note>) : NotesState
}
