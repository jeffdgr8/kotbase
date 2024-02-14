package data.source.user

import kotbase.Document
import kotbase.MutableDocument
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class UserDoc(
    val userId: String = "",
    // NOTE: not recommended in production, save credentials to secure storage
    val password: String = ""
)

fun decodeDocument(doc: Document?): UserDoc? {
    return doc?.toJSON()?.let { json ->
        Json.decodeFromString<UserDoc>(json)
    }
}

fun UserDoc.toMutableDocument(id: String): MutableDocument {
    val json = Json.encodeToString(this)
    return MutableDocument(id, json)
}
