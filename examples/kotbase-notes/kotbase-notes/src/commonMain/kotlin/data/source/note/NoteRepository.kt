package data.source.note

import data.db.DatabaseProvider
import data.db.UserScopeProvider
import data.source.user.UserRepository
import domain.model.Note
import domain.model.User
import kotbase.Collection
import kotbase.Expression
import kotbase.From
import kotbase.FullTextFunction
import kotbase.FullTextIndexConfiguration
import kotbase.Function
import kotbase.Meta
import kotbase.MutableDocument
import kotbase.OrderBy
import kotbase.Ordering
import kotbase.ktx.asObjectsFlow
import kotbase.ktx.from
import kotbase.ktx.orderBy
import kotbase.ktx.select
import kotbase.ktx.where
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds

class NoteRepository(
    private val dbProvider: DatabaseProvider,
    private val userScopeProvider: UserScopeProvider,
    userRepository: UserRepository
) {

    init {
        userRepository.user
            .filterIsInstance<User.Authenticated>()
            .onEach {
                dbCollection.createIndex(FTS_INDEX, FullTextIndexConfiguration("title", "text"))
            }
            .launchIn(dbProvider.scope)
    }

    val dbCollection: Collection
        get() = userScopeProvider.createUserScopeCollection(COLLECTION_NAME)

    fun getNotesFlow(searchTerms: String, useFts: Boolean): Flow<List<Note>> {
        val baseQuery = select(Meta.id, "title", "text", "info.modified")
            .from(dbCollection)

        val query = if (searchTerms.isNotBlank()) {
            val lowercaseTerms = searchTerms.lowercase()
            if (useFts) {
                baseQuery.ftsWhereAndOrder(lowercaseTerms)
            } else {
                baseQuery.whereAndOrder(lowercaseTerms)
            }
        } else {
            baseQuery.orderBy { "info.modified".descending() }
        }

        return query
            .asObjectsFlow { json: String ->
                Json.decodeFromString<Note>(json)
            }
    }

    private fun From.whereAndOrder(searchTerms: String): OrderBy {
        return where {
            searchTerms.split("""\s+""".toRegex()).map { term ->
                (Function.lower(Expression.property("title")) like Expression.string("%$term%")) or
                (Function.lower(Expression.property("text")) like Expression.string("%$term%"))
            }.reduce { acc, exp ->
                acc and exp
            }
        }
        .orderBy { "info.modified".descending() }
    }

    private fun From.ftsWhereAndOrder(searchTerms: String): OrderBy {
        val ftsIndex = Expression.fullTextIndex(FTS_INDEX)

        return where(
            FullTextFunction.match(ftsIndex, searchTerms)
        )
        .orderBy(Ordering.expression(FullTextFunction.rank(ftsIndex)))
    }

    suspend fun getNote(noteId: String): Note? {
        return withContext(dbProvider.readContext) {
            dbCollection.getDocument(noteId)
                ?.let(::decodeDocument)
                ?.let(::noteDocMapper)
        }
    }

    private fun noteDocMapper(doc: NoteDoc): Note {
        return Note(
            id = doc.id,
            title = doc.title,
            text = doc.text,
            modified = doc.info.modified
        )
    }

    private val saveChannel = Channel<Note>(Channel.CONFLATED)

    fun save(note: Note) {
        saveChannel.trySend(note)
    }

    init {
        @OptIn(FlowPreview::class)
        saveChannel.receiveAsFlow()
            .debounce(500.milliseconds)
            .onEach { note ->

                val coll = dbCollection
                val doc = coll.getDocument(note.id)
                    ?.let(::decodeDocument)
                    ?: run {
                        val userId = userScopeProvider.userId ?: userNotLoggedInError()
                        NoteDoc(
                            info = NoteDoc.Info(
                                author = userId,
                                created = note.modified
                            )
                        )
                    }

                val updated = doc.copy(
                    title = note.title,
                    text = note.text,
                    info = doc.info.copy(modified = note.modified)
                )

                val json = Json.encodeToString(updated)
                val mutableDoc = MutableDocument(note.id, json)

                coll.save(mutableDoc)
            }
            .launchIn(dbProvider.scope)
    }

    fun delete(note: Note) {
        dbProvider.scope.launch {
            val coll = dbCollection
            coll.getDocument(note.id)?.let {
                coll.delete(it)
            }
        }
    }

    private fun userNotLoggedInError(): Nothing = error("User not logged in")

    companion object {
        const val COLLECTION_NAME = "notes"
        const val FTS_INDEX = "search-index"
    }
}
