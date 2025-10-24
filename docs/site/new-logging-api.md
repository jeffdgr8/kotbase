_Couchbase Lite 3.2.2 introduces a new Logging API._

## Upgrading to the New CBL Logging API

!!! warning

    Use of the deprecated and new Logging API at the same time is not supported.

You can find information about the new Couchbase Lite Logging API introduced in Couchbase Lite 3.2.2.

For information about the now deprecated earlier version of the Logging API, see [Legacy Logging API](
legacy-logging-api.md).

### LogSinks

Couchbase Lite 3.2.2 introduces a new Logging API. The new Logging API has the following benefits:

* Log sinks are now thread safe, removing risk of inconsistent states during initialization.
* Simplified API and reduced implementation complexity.

The new logging API retains many of the core concepts of the previous API.

The first thing to note is that the three destinations for logs have been renamed as `LogSinks`, in keeping with common
source/sink terminology.

The `FileLogSink`, the `ConsoleLogSink` and the `CustomLogSink` are all to be installed in the `LogSinks` object.

Only `ConsoleLogSink` is enabled for all logging domains at the warning level by default. To enable a specific type of
log sink, create a log sink object of that type, set its minimum log level and domains, and assign it to `LogSinks`. To
disable a log sink, set it to null or use a log sink with `LogLevel.NONE`.

Couchbase still logs its messages in a handful of named domains and at common log levels: `LogLevel.DEBUG` the most
verbose, and `LogLevel.ERROR` only for serious failures.

The biggest difference between the new and the old API is that `LogSinks` are immutable: you set the level and domain at
which they log in their constructors. For example, you can only change the level at which the `ConsoleLogSink` forwards
messages to the console by installing a new one created for the new log level.

Log output is split into the following streams:

* [Logging to the Couchbase File Log](#logging-to-the-couchbase-file-log)
  Each log level writes to a separate file and there is a single retention policy for all files.
* [Logging to the Console](#logging-to-the-console)
  You can independently configure and control console logs, which provides a convenient method of accessing diagnostic
  information during debugging scenarios.

  With console logging, you can fine-tune diagnostic output to suit specific debug scenarios, without interfering with
  any logging required by Couchbase Support for the investigation of issues.
* [Using a Custom Logger](#using-a-custom-logger)
  For greater flexibility you can implement a custom logging class.

### Logging to the Console

The changes necessary convert the installation of a console logger from the old to the new API are minimal. Create an
instance of `ConsoleLogSink` initialized with the desired log level and domains and install it.

**Old API**

```kotlin
Database.log.console.domains = LogDomain.ALL_DOMAINS
Database.log.console.level = LogLevel.WARNING
```

**New API**

```kotlin
LogSinks.console = ConsoleLogSink(LogLevel.WARNING)
```

### Logging to the Couchbase File Log

The changes necessary to convert the installation of a file logger are also similar. Instead of configuring a
`FileLogger` using a `LogFileConfiguration`, create a new `FileLogSink` with the desired properties and install it.

!!! note

    `setRotateCount` from the old API is slightly different from `setMaxKeptFiles`. `setMaxKeptFiles` is the maximum
    number of log files that will exist at any time and is the count of rotated files (`setRotateCount`) plus one.

**Old API**

```kotlin
Database.log.file.apply {
    config = LogFileConfiguration(directory = "path/to/temp/logs").apply {
        maxSize = 10240
        maxRotateCount = 5
        usesPlaintext = false
    }
    level = LogLevel.INFO
}
```

**New API**

```kotlin
LogSinks.file = FileLogSink(
    directory = "path/to/temp/logs",
    maxKeptFiles = 12,
    isPlainText = true
)
```

### Using a Custom Logger

Installing a custom log sink with the new API is also streamlined: create an instance of your custom sink, and to
install it use `LogSinks.custom`.

As with the other log sinks, you will have to specify the level and domain at which Couchbase logs are forwarded to your
custom sink at its creation.

Your custom log sink code will have to change as well. A custom log sink implements the `LogSink` functional interface
and is assigned to a `CustomLogSink` instance during creation.

A second important change is that your logger will receive only logs at the level and domain for which it is
initialized. There is no need to record or filter the logs forwarded to the `writeLog` method which replaces the `log`
method from the old API.

Related to this last point, the Couchbase `Logger`s, now `LogSink`s are meant to support logging by the Couchbase Lite
platform. They were never meant as a general framework for logging.

!!! important

    With the new API, customer code can no longer log, directly, to any of the Couchbase log sinks. The Console and File
    log sinks cannot be subclassed and do not publish methods that allow writing logs. If you need to log to the console
    for example, you’ll have to create your own way of doing so.

**Old API Implementing The Custom Logger Interface**

```kotlin
class LogTestLogger(override val level: LogLevel) : Logger {
    override fun log(level: LogLevel, domain: LogDomain, message: String) {
        // this method will never be called if param level < this.level
        // handle the message, for example piping it to a third party framework
    }
}
```

**Old API Enable Custom Logger**

```kotlin
// this custom logger will not log an event with a log level < WARNING
Database.log.custom = LogTestLogger(LogLevel.WARNING)
```

**New API**

```kotlin
LogSinks.custom = CustomLogSink(
    LogLevel.WARNING,
    LogDomain.NETWORK, LogDomain.REPLICATOR
) { level, domain, message ->
    // will be called only with messages from the NETWORK and REPLICATOR
    // domains with a log level of WARNING or higher.
}
```
