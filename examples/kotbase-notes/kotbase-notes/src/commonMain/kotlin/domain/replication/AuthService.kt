package domain.replication

import data.db.DatabaseProvider
import data.source.user.UserRepository
import domain.model.User
import io.ktor.client.HttpClient
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.http.*
import io.ktor.utils.io.errors.*
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
            when (it) {
                is User.Authenticated -> AuthStatus.LoggedIn
                User.None -> AuthStatus.LoggedOut
                User.Unknown -> AuthStatus.Unknown
            }
        }
        .stateIn(dbProvider.scope, SharingStarted.Eagerly, AuthStatus.Unknown)

    suspend fun authenticateUser(username: String, password: String): Result<Boolean> {
        val result = try {
            val status = HttpClient().get(syncGateway.httpEndpoint) {
                basicAuth(username, password)
            }
            .status

            when {
                status.isSuccess() -> true
                status == HttpStatusCode.Unauthorized -> false
                else -> return Result.failure(IllegalStateException(status.description))
            }
        } catch (e: IOException) {
            return Result.failure(e)
        }

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
