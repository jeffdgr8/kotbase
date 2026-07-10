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
@Deprecated("Use CollectionConfiguration()")
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
@Deprecated(
    "Use CollectionConfiguration()",
    ReplaceWith("CollectionConfiguration(channels, documentIDs, pullFilter, pushFilter, conflictResolver)")
)
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
@Deprecated("Use FullTextIndexConfiguration()")
public val FullTextIndexConfigurationFactory: FullTextIndexConfiguration? = null

/**
 * Create a FullTextIndexConfiguration, overriding the receiver's
 * values with the passed parameters:
 *
 * @param expressions (required) the expressions to be matched.
 *
 * @see FullTextIndexConfiguration
 */
@Deprecated(
    "Use FullTextIndexConfiguration()",
    ReplaceWith("FullTextIndexConfiguration(expressions.asList(), ignoreAccents = ignoreAccents, language = language)")
)
public fun FullTextIndexConfiguration?.newConfig(
    vararg expressions: String,
    language: String? = null,
    ignoreAccents: Boolean? = null
): FullTextIndexConfiguration {
    return FullTextIndexConfiguration(
        requireNotNull(
            expressions.asList().ifEmpty { this?.expressions }
        ) { "A FullTextIndexConfiguration must specify expressions" },
        ignoreAccents = ignoreAccents ?: this?.isIgnoringAccents ?: Defaults.FullTextIndex.IGNORE_ACCENTS,
        language = language ?: this?.language ?: FullTextIndexConfiguration.NOT_SPECIFIED
    )
}

/**
 * Configuration factory for new ValueIndexConfigurations
 *
 * Usage:
 *
 *     val valIndexConfig = ValueIndexConfigurationFactory.newConfig(...)
 */
@Deprecated("Use ValueIndexConfiguration()")
public val ValueIndexConfigurationFactory: ValueIndexConfiguration? = null

/**
 * Create a ValueIndexConfiguration, overriding the receiver's
 * values with the passed parameters:
 *
 * @param expressions (required) the expressions to be matched.
 *
 * @see ValueIndexConfiguration
 */
@Deprecated(
    "Use ValueIndexConfiguration()",
    ReplaceWith("ValueIndexConfiguration(expressions.asList())")
)
public fun ValueIndexConfiguration?.newConfig(
    vararg expressions: String
): ValueIndexConfiguration {
    return ValueIndexConfiguration(
        requireNotNull(
            expressions.asList().ifEmpty { this?.expressions }
        ) { "A ValueIndexConfiguration must specify expressions" }
    )
}
