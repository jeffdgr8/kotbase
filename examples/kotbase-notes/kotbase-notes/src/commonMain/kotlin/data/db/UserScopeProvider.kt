package data.db

import data.source.user.UserRepository
import kotbase.Collection

class UserScopeProvider(
    private val dbProvider: DatabaseProvider,
    private val userRepository: UserRepository
) {

    val userId: String?
        get() = userRepository.userId

    private val userIdNotNull: String
        get() = requireNotNull(userId) { "User is not logged in" }

    fun createUserScopeCollection(collectionName: String): Collection {
        return dbProvider.database.createCollection(collectionName, userScopeName(userIdNotNull))
    }

    private fun userScopeName(userId: String): String =
        "user-$userId"
}
