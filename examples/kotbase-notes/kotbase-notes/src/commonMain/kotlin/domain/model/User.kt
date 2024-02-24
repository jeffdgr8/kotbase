package domain.model

sealed interface User {
    data class Authenticated(val userId: String, val password: String) : User
    data object None : User
    data object Unknown : User
}
