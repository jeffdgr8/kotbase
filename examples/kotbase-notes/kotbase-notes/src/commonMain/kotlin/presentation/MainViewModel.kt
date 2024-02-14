package presentation

import data.source.note.NoteRepository
import domain.replication.AuthService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class MainViewModel(
    scope: CoroutineScope,
    private val noteRepository: NoteRepository,
    private val authService: AuthService
) {

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
    val notes = combine(searchText, useFts) { searchText, useFts ->
            Pair(searchText, useFts)
        }
        .flatMapLatest { (searchText, useFts) ->
            noteRepository.getNotesFlow(searchText, useFts)
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun logout() {
        authService.logout()
    }
}
