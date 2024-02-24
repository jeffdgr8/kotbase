package data.source.user

import data.db.DatabaseProvider
import domain.model.User
import kotbase.Collection
import kotbase.ktx.documentFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class UserRepository(
    private val dbProvider: DatabaseProvider
) {

    private val dbCollection: Collection
        get() = dbProvider.database.defaultCollection

    val user: StateFlow<User> =
        dbCollection.documentFlow(USER_DOC_ID, dbProvider.readContext)
            .map(::decodeDocument)
            .map { userDoc ->
                userDoc?.let {
                    User.Authenticated(it.userId, it.password)
                } ?: User.None
            }
            .stateIn(dbProvider.scope, SharingStarted.Eagerly, User.Unknown)

    val userId: String?
        get() = when (val user = user.value) {
            is User.Authenticated -> user.userId
            else -> null
        }

    suspend fun saveUser(userId: String, password: String): Boolean {
        withContext(dbProvider.writeContext) {
            val doc = UserDoc(userId, password)
                .toMutableDocument(USER_DOC_ID)
            dbCollection.save(doc)
        }
        return true
    }

    fun deleteUser() {
        dbProvider.scope.launch {
            dbCollection.getDocument(USER_DOC_ID)?.let {
                dbCollection.delete(it)
            }
        }
    }

    companion object {
        private const val USER_DOC_ID = "user"
    }
}
