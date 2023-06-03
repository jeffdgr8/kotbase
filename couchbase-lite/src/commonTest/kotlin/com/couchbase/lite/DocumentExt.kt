package com.couchbase.lite

import kotbase.Dictionary
import kotbase.Document

internal expect val Document.content: Dictionary

internal expect fun Document.exists(): Boolean
