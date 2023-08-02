package kotbase

import libcblite.CBLDocumentFlags
import libcblite.kCBLDocumentFlagsAccessRemoved
import libcblite.kCBLDocumentFlagsDeleted

public actual enum class DocumentFlag {
    DELETED,
    ACCESS_REMOVED
}

internal fun CBLDocumentFlags.toDocumentFlags(): Set<DocumentFlag> = buildSet {
    val flags = this@toDocumentFlags
    if (flags and kCBLDocumentFlagsDeleted != 0U) {
        add(DocumentFlag.DELETED)
    }
    if (flags and kCBLDocumentFlagsAccessRemoved != 0U) {
        add(DocumentFlag.ACCESS_REMOVED)
    }
}

internal fun Set<DocumentFlag>.toCBLDocumentFlags(): CBLDocumentFlags {
    var flags = 0U
    if (contains(DocumentFlag.DELETED)) {
        flags = flags or kCBLDocumentFlagsDeleted
    }
    if (contains(DocumentFlag.ACCESS_REMOVED)) {
        flags = flags or kCBLDocumentFlagsAccessRemoved
    }
    return flags
}
