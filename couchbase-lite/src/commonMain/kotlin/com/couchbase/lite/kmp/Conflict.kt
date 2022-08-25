@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp

public expect class Conflict {

    public val documentId: String

    public val localDocument: Document?

    public val remoteDocument: Document?
}
