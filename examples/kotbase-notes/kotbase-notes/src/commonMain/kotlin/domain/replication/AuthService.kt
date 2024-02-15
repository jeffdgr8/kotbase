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
        .stateIn(dbProvider.scope, SharingStarted.Eagerly, AuthStatus.Unknown)

    suspend fun authenticateUser(username: String, password: String): Result<Boolean> {
        val result = runCatching {
            HttpClient().get(syncGateway.httpEndpoint) {
                basicAuth(username, password)
            }
            .status == HttpStatusCode.OK
        }.getOrElse { return Result.failure(it) }

        if (result) {
            userRepository.saveUser(username, password)
        }

        return Result.success(result)
    }

    fun logout() {
        userRepository.deleteUser()
    }
}

enum class AuthStatus {
    LoggedIn, LoggedOut, Unknown
}
