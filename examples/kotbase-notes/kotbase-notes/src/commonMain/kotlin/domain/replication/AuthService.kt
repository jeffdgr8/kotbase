package domain.replication

import data.db.DatabaseProvider
import data.source.user.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AuthService(
    private val syncGateway: SyncGateway,
    dbProvider: DatabaseProvider,
    private val userRepository: UserRepository
) {

    val authStatus: StateFlow<AuthStatus> =
        userRepository.user.map {
            when {
                it == null -> AuthStatus.LoggedOut
                it.userId.isBlank() -> AuthStatus.Unknown
                else -> AuthStatus.LoggedIn
            }
        }
        .stateIn(dbProvider.writeScope, SharingStarted.Eagerly, AuthStatus.Unknown)

    suspend fun authenticateUser(username: String, password: String): Boolean {
        val result = HttpClient()
            .get(syncGateway.httpEndpoint) {
                basicAuth(username, password)
            }
            .status == HttpStatusCode.OK

        if (result) {
            userRepository.saveUser(username, password)
        }

        return result
    }

    fun logout() {
        userRepository.deleteUser()
    }
}

enum class AuthStatus {
    LoggedIn, LoggedOut, Unknown
}
