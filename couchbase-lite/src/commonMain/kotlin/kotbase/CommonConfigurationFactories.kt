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

/**
 * Configuration factory for new CollectionConfigurations
 *
 * Usage:
 *
 *     val collConfig = CollectionConfigurationFactory.newConfig(...)
 */
public val CollectionConfigurationFactory: CollectionConfiguration? = null

/**
 * Create a CollectionConfiguration, overriding the receiver's
 * values with the passed parameters:
 *
 * @param channels Sync Gateway channel names.
 * @param documentIDs IDs of documents to be replicated: default is all documents.
 * @param pullFilter filter for pulled documents.
 * @param pushFilter filter for pushed documents.
 * @param conflictResolver conflict resolver.
 *
 * @see CollectionConfiguration
 */
public fun CollectionConfiguration?.newConfig(
    channels: List<String>? = null,
    documentIDs: List<String>? = null,
    pullFilter: ReplicationFilter? = null,
    pushFilter: ReplicationFilter? = null,
    conflictResolver: ConflictResolver? = null
): CollectionConfiguration {
    val config = CollectionConfiguration()

    (channels ?: this?.channels)?.let { config.channels = it }
    (documentIDs ?: this?.documentIDs)?.let { config.documentIDs = it }
    (pushFilter ?: this?.pushFilter)?.let { config.pushFilter = it }
    (pullFilter ?: this?.pullFilter)?.let { config.pullFilter = it }
    (conflictResolver ?: this?.conflictResolver)?.let { config.conflictResolver = it }

    return config
}

/**
 * Configuration factory for new FullTextIndexConfigurations
 *
 * Usage:
 *
 *     val fullTextIndexConfig = FullTextIndexConfigurationFactory.newConfig(...)
 */
public val FullTextIndexConfigurationFactory: FullTextIndexConfiguration? = null

/**
 * Create a FullTextIndexConfiguration, overriding the receiver's
 * values with the passed parameters:
 *
 * @param expressions (required) the expressions to be matched.
 *
 * @see FullTextIndexConfiguration
 */
public fun FullTextIndexConfiguration?.newConfig(
    vararg expressions: String = emptyArray(),
    language: String? = null,
    ignoreAccents: Boolean? = null
): FullTextIndexConfiguration {
    return FullTextIndexConfiguration(
        *requireNotNull(
            if (expressions.isNotEmpty()) {
                expressions
            } else {
                this?.expressions?.toTypedArray()
            }
        ) { "A FullTextIndexConfiguration must specify expressions" }
    ).apply {
        (language ?: this@newConfig?.language)?.let { this.language = it }
        (ignoreAccents ?: this@newConfig?.isIgnoringAccents)?.let { this.isIgnoringAccents = it }
    }
}

/**
 * Configuration factory for new ValueIndexConfigurations
 *
 * Usage:
 *
 *     val valIndexConfig = ValueIndexConfigurationFactory.newConfig(...)
 */
public val ValueIndexConfigurationFactory: ValueIndexConfiguration? = null

/**
 * Create a ValueIndexConfiguration, overriding the receiver's
 * values with the passed parameters:
 *
 * @param expressions (required) the expressions to be matched.
 *
 * @see ValueIndexConfiguration
 */
public fun ValueIndexConfiguration?.newConfig(
    vararg expressions: String = emptyArray()
): ValueIndexConfiguration {
    return ValueIndexConfiguration(
        *requireNotNull(
            if (expressions.isNotEmpty()) {
                expressions
            } else {
                this?.expressions?.toTypedArray()
            }
        ) { "A ValueIndexConfiguration must specify expressions" }
    )
}

/**
 * Configuration factory for new LogFileConfigurations
 *
 * Usage:
 *
 *     val logFileConfig = LogFileConfigurationFactory.newConfig(...)
 */
public val LogFileConfigurationFactory: LogFileConfiguration? = null

/**
 * Create a LogFileConfiguration, overriding the receiver's
 * values with the passed parameters:
 *
 * @param directory (required) the directory in which the logs files are stored.
 * @param maxSize the max size of the log file in bytes.
 * @param maxRotateCount the number of rotated logs that are saved.
 * @param usePlainText whether or not to log in plaintext.
 *
 * @see LogFileConfiguration
 */
public fun LogFileConfiguration?.newConfig(
    directory: String? = null,
    maxSize: Long? = null,
    maxRotateCount: Int? = null,
    usePlainText: Boolean? = null
): LogFileConfiguration {
    return LogFileConfiguration(
        requireNotNull(directory ?: this?.directory) { "Must specify a db directory" },
        this
    ).apply {
        maxSize?.let { setMaxSize(it) }
        maxRotateCount?.let { setMaxRotateCount(it) }
        usePlainText?.let { setUsePlaintext(it) }
    }
}
