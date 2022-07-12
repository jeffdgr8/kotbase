package com.couchbase.lite.kmm.internal

import kotlinx.cinterop.convert
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_queue_t
import platform.posix.QOS_CLASS_DEFAULT

internal var useTestQueue = false

internal val testQueue: dispatch_queue_t
    get() = dispatch_get_global_queue(QOS_CLASS_DEFAULT.convert(), 0)
