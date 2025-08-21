package data.source.note

import kotbase.Document
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json

@Serializable
data class NoteDoc(
    @Transient
    val id: String = "",
    val title: String = "",
    val text: String = "",
    val info: Info = Info()
) {
    @Serializable
    data class Info(
        val author: String = "",
        val created: Instant = Clock.System.now(),
        val modified: Instant = Clock.System.now()
    )
}

fun decodeDocument(doc: Document?): NoteDoc? {
    return doc?.toJSON()?.let { json ->
        Json.decodeFromString<NoteDoc>(json)
            .copy(id = doc.id)
    }
}
