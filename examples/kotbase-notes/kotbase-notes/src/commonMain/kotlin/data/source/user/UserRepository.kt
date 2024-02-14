package data.source.user

import data.db.DatabaseProvider
import kotbase.Collection
import kotbase.ktx.documentFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class UserRepository(
    private val dbProvider: DatabaseProvider
) {

    private val dbCollection: Collection
        get() = dbProvider.database.defaultCollection

    val user: StateFlow<UserDoc?> =
        dbCollection.documentFlow(USER_DOC_ID, dbProvider.readContext)
            .map(::decodeDocument)
            .stateIn(dbProvider.scope, SharingStarted.Eagerly, UserDoc())

    val userId: String?
        get() = user.value?.userId?.ifBlank { null }

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
