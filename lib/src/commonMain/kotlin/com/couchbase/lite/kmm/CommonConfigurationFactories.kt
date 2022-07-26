package com.couchbase.lite.kmm

/**
 * Configuration factory for new FullTextIndexConfigurations
 *
 * Usage:
 *
 *      val fullTextIndexConfig = FullTextIndexConfigurationFactory.create(...)
 */
public val FullTextIndexConfigurationFactory: FullTextIndexConfiguration? = null

/**
 * Create a FullTextIndexConfiguration, overriding the receiver's
 * values with the passed parameters:
 *
 * @param expressions (required) the expressions to be matched.
 *
 * @see com.couchbase.lite.kmm.FullTextIndexConfiguration
 */
public fun FullTextIndexConfiguration?.create(
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
        ) { "Must specify an expression" }
    ).apply {
        (language ?: this@create?.language)?.let { this.language = it }
        (ignoreAccents ?: this@create?.isIgnoringAccents)?.let { this.isIgnoringAccents = it }
    }
}

/**
 * Configuration factory for new ValueIndexConfigurations
 *
 * Usage:
 *
 *     val valIndexConfig = ValueIndexConfigurationFactory.create(...)
 */
public val ValueIndexConfigurationFactory: ValueIndexConfiguration? = null

/**
 * Create a FullTextIndexConfiguration, overriding the receiver's
 * values with the passed parameters:
 *
 * @param expressions (required) the expressions to be matched.
 *
 * @see com.couchbase.lite.kmm.ValueIndexConfiguration
 */
public fun ValueIndexConfiguration?.create(
    vararg expressions: String = emptyArray()
): ValueIndexConfiguration {
    return ValueIndexConfiguration(
        *requireNotNull(
            if (expressions.isNotEmpty()) {
                expressions
            } else {
                this?.expressions?.toTypedArray()
            }
        ) { "Must specify an expression" }
    )
}

/**
 * Configuration factory for new LogFileConfigurations
 *
 * Usage:
 *
 *      val logFileConfig = LogFileConfigurationFactory.create(...)
 */
public val LogFileConfigurationFactory: LogFileConfiguration? = null

/**
 * Create a FullTextIndexConfiguration, overriding the receiver's
 * values with the passed parameters:
 *
 * @param directory (required) the directory in which the logs files are stored.
 * @param maxSize the max size of the log file in bytes.
 * @param maxRotateCount the number of rotated logs that are saved.
 * @param usePlainText whether or not to log in plaintext.
 *
 * @see com.couchbase.lite.kmm.LogFileConfiguration
 */
public fun LogFileConfiguration?.create(
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
