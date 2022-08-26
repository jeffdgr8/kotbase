@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite

import com.couchbase.lite.kmp.Log

internal expect fun Log.reset()
