package com.couchbase.lite

import com.couchbase.lite.kmp.Dictionary
import com.couchbase.lite.kmp.Document

internal expect val Document.content: Dictionary

internal expect fun Document.exists(): Boolean
