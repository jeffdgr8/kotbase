package com.couchbase.lite

import com.couchbase.lite.kmm.Dictionary
import com.couchbase.lite.kmm.Document

internal expect val Document.content: Dictionary

internal expect fun Document.exists(): Boolean

internal expect fun Document.generation(): Long
