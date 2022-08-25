@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite

import com.couchbase.lite.kmp.Dictionary
import com.couchbase.lite.kmp.Document

internal expect val Document.content: Dictionary

internal expect fun Document.exists(): Boolean

internal expect fun Document.generation(): Long
