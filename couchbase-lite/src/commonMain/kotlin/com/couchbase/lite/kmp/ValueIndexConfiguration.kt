@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp

public expect class ValueIndexConfiguration(vararg expressions: String) : IndexConfiguration
