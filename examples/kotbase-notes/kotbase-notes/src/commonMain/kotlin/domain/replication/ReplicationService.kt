package domain.replication

import data.db.DatabaseProvider
import data.source.note.NoteRepository
import data.source.user.UserRepository
import kotbase.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class ReplicationService(
    dbProvider: DatabaseProvider,
    private val noteRepository: NoteRepository,
    private val userRepository: UserRepository,
    private val syncGateway: SyncGateway
) {

    init {
        userRepository.user
            .onEach { user ->
                replicator?.stop()
                replicatorFlow.value = if (user?.userId?.isNotBlank() == true) {
                    createReplicator(user.userId, user.password)
                } else null
                checkStart()
            }
            .launchIn(dbProvider.scope + Dispatchers.Default)
    }

    private val replicatorFlow = MutableStateFlow<Replicator?>(null)

    private val replicator: Replicator?
        get() = replicatorFlow.value

    private fun createReplicator(user: String, password: String): Replicator {
        val targetEndpoint = URLEndpoint(syncGateway.wsEndpoint)
        val config = ReplicatorConfiguration(targetEndpoint).apply {
            addCollection(noteRepository.dbCollection, null)
            type = ReplicatorType.PUSH_AND_PULL
            isContinuous = true
            authenticator = BasicAuthenticator(user, password.toCharArray())
        }
        return Replicator(config)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val replicationStatus: StateFlow<ReplicatorStatus?> =
        replicatorFlow.flatMapLatest {
            it?.replicatorChangesFlow()
                ?: flowOf(null)
        }
        .map { it?.status }
        .onEach {
            it?.error?.run {
                if (domain == CBLError.Domain.CBLITE && code == CBLError.Code.HTTP_AUTH_REQUIRED) {
                    userRepository.deleteUser()
                }
            }
        }
        .stateIn(dbProvider.scope, SharingStarted.Eagerly, null)

    private var started = false

    fun startReplication() {
        started = true
        replicator?.start()
    }

    fun stopReplication() {
        started = false
        replicator?.stop()
    }

    private suspend fun checkStart() {
        withContext(Dispatchers.Main) {
            if (started) replicator?.start()
        }
    }
}
