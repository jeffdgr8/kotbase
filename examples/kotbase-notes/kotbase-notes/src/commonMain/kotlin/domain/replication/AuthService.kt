package domain.replication

import data.source.user.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode

class AuthService(
    private val syncGateway: SyncGateway,
    private val userRepository: UserRepository
) {

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
