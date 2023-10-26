/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

import cocoapods.CouchbaseLite.CBLLogFileConfiguration
import kotbase.internal.DelegatedClass
import kotlinx.cinterop.convert

public actual class LogFileConfiguration
internal constructor(actual: CBLLogFileConfiguration) : DelegatedClass<CBLLogFileConfiguration>(actual) {

    public actual constructor(directory: String) : this(CBLLogFileConfiguration(directory))

    public actual constructor(config: LogFileConfiguration) : this(config.directory, config)

    public actual constructor(directory: String, config: LogFileConfiguration?) : this(directory) {
        config?.let {
            setMaxRotateCount(it.maxRotateCount)
            setMaxSize(it.maxSize)
            setUsePlaintext(it.usesPlaintext)
        }
    }

    internal constructor(actual: CBLLogFileConfiguration, readonly: Boolean) : this(actual) {
        this.readonly = readonly
    }

    internal var readonly: Boolean = false

    public actual fun setUsePlaintext(usePlaintext: Boolean): LogFileConfiguration {
        checkReadOnly()
        actual.setUsePlainText(usePlaintext)
        return this
    }

    public actual var maxRotateCount: Int
        get() = actual.maxRotateCount.toInt()
        set(value) {
            checkReadOnly()
            actual.maxRotateCount = value.convert()
        }

    public actual fun setMaxRotateCount(maxRotateCount: Int): LogFileConfiguration {
        checkReadOnly()
        actual.setMaxRotateCount(maxRotateCount.convert())
        return this
    }

    public actual var maxSize: Long
        get() = actual.maxSize.toLong()
        set(value) {
            checkReadOnly()
            actual.maxSize = value.convert()
        }

    public actual fun setMaxSize(maxSize: Long): LogFileConfiguration {
        checkReadOnly()
        actual.setMaxSize(maxSize.convert())
        return this
    }

    public actual var usesPlaintext: Boolean
        get() = actual.usePlainText
        set(value) {
            checkReadOnly()
            actual.usePlainText = value
        }

    public actual val directory: String
        get() = actual.directory

    // Objective-C SDK doesn't override these, but Java does and tests expect
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LogFileConfiguration) return false
        return maxRotateCount == other.maxRotateCount
                && directory == other.directory
                && maxSize == other.maxSize
                && usesPlaintext == other.usesPlaintext
    }

    override fun hashCode(): Int = directory.hashCode()

    private fun checkReadOnly() {
        if (readonly) throw IllegalStateException("LogFileConfiguration is readonly mode.")
    }
}

internal fun CBLLogFileConfiguration.asReadOnlyLogFileConfiguration() =
    LogFileConfiguration(this, true)
