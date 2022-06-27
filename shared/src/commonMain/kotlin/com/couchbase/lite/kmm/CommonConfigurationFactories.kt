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
public expect fun FullTextIndexConfiguration?.create(
    language: String? = null,
    ignoreAccents: Boolean? = null,
    vararg expressions: String = emptyArray()
): FullTextIndexConfiguration

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
public expect fun ValueIndexConfiguration?.create(
    vararg expressions: String = emptyArray()
): ValueIndexConfiguration

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
        directory ?: this?.getDirectory() ?: error("Must specify a db directory"),
        this
    ).apply {
        maxSize?.let { setMaxSize(it) }
        maxRotateCount?.let { setMaxRotateCount(it) }
        usePlainText?.let { setUsePlaintext(it) }
    }
}
