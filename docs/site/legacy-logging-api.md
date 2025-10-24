_Couchbase Lite — Using Logs for Troubleshooting_

!!! note "Constraints"

    The retrieval of logs from the device is out of scope of this feature.

## Introduction

Couchbase Lite provides a robust Logging API — see API References for [`Log`](/api/couchbase-lite-ee/kotbase/-log/),
[`FileLogger`](/api/couchbase-lite-ee/kotbase/-file-logger/), and [`LogFileConfiguration`](
/api/couchbase-lite-ee/kotbase/-log-file-configuration/) — which make debugging and troubleshooting easier during
development and in production. It delivers flexibility in terms of how logs are generated and retained, whilst also
maintaining the level of logging required by Couchbase Support for investigation of issues.

Log output is split into the following streams:

* [Console based logging](#console-based-logging)<br><br>
  You can independently configure and control console logs, which provides a convenient method of accessing diagnostic
  information during debugging scenarios. With console logging, you can fine-tune diagnostic output to suit specific
  debug scenarios and capture them for Couchbase Support for the investigation of issues.
* [File based logging](#file-based-logging)<br><br>
  Here logs are written to separate log files, filtered by log level, with each log level supporting individual
  retention policies.
* [Custom logging](#custom-logging)<br><br>
  For greater flexibility you can implement a custom logging class using the [`Logger`](
  /api/couchbase-lite-ee/kotbase/-logger/) interface.

In all instances, you control what is logged and at what level using the [`Log`](/api/couchbase-lite-ee/kotbase/-log/)
class.

## Console based logging

Console based logging is often used to facilitate troubleshooting during development.

Console logs are your go-to resource for diagnostic information. You can easily fine-tune their diagnostic content to
meet the needs of a particular debugging scenario, perhaps by increasing the verbosity and-or choosing to focus on
messages from a specific domain; to better focus on the problem area.

Changes to console logging are independent of file logging, so you can make changes without compromising any file
logging streams. It is enabled by default. To change default settings use the [`Database.log`](
/api/couchbase-lite-ee/kotbase/-database/-companion/log.html) property to set the required values — see [Example 1
](#xample-1).

You will primarily use [`log.console`](/api/couchbase-lite-ee/kotbase/-log/console.html) and [`ConsoleLogger`](
/api/couchbase-lite-ee/kotbase/-console-logger/) to control console logging.

!!! example "<span id='example-1'>Example 1. Change Console Logging Settings</span>"

    This example enables and defines console-based logging settings.

    ```kotlin
    Database.log.console.domains = LogDomain.ALL_DOMAINS
    Database.log.console.level = LogLevel.VERBOSE
    ```

1. Define the required domain; here we turn on logging for all available domains — see [`ConsoleLogger.domains`](
   /api/couchbase-lite-ee/kotbase/-console-logger/domains.html) and enum [`LogDomain`](
   /api/couchbase-lite-ee/kotbase/-log-domain/).
2. Here we turn on the most verbose log level — see [`ConsoleLogger.level`](
   /api/couchbase-lite-ee/kotbase/-console-logger/level.html) and enum [`LogLevel`](
   /api/couchbase-lite-ee/kotbase/-log-level/).  
   To disable logging for the specified `LogDomain`s set the `LogLevel` to `NONE`.

## File based logging

File based logging is disabled by default — see [Example 2](#example-2) for how to enable it.

You will primarily use [`Log.file`](/api/couchbase-lite-ee/kotbase/-log/file.html) and [`FileLogger`](
/api/couchbase-lite-ee/kotbase/-file-logger/) to control file-based logging.

### Formats

Available file based logging formats:

* Binary — most efficient for storage and performance. It is the default for file based logging.<br><br.
  Use this format and a decoder, such as **cbl-log**, to view them — see [Decoding binary logs](#decoding-binary-logs).
* Plaintext

### Configuration

As with console logging you can set the log level — see the [`FileLogger`](/api/couchbase-lite-ee/kotbase/-file-logger/)
class.

With file based logging you can also use the [`LogFileConfiguration`](
/api/couchbase-lite-ee/kotbase/-log-file-configuration/) class’s properties to specify the:

* Path to the directory to store the log files
* Log file format  
  The default is _binary_. You can override that where necessary and output a plain text log.
* Maximum number of rotated log files to keep
* Maximum size of the log file (bytes). Once this limit is exceeded a new log file is started.

!!! example "<span id='example-2'>Example 2. Enabling file logging</span>"

    ```kotlin
    Database.log.file.apply {
        config = LogFileConfiguration(directory = "temp/cbl-logs").apply {
            maxSize = 10240
            maxRotateCount = 5
            usesPlaintext = false
        }
        level = LogLevel.INFO
    }
    ```

1. Set the log file directory
2. Change the max rotation count from the default (1) to 5  
   **Note** this means six files may exist at any one time; the five rotated log files, plus the active log file
3. Set the maximum size (bytes) for our log file
4. Select the binary log format (included for reference only as this is the default)
5. Increase the log output level from the default (`WARNING`) to `INFO` — see [`FileLogger.level`](
   /api/couchbase-lite-ee/kotbase/-file-logger/level.html)

!!! tip

    `"temp/cbl-logs"` might be a platform-specific location. Use [`expect`/`actual`](
    https://kotlinlang.org/docs/multiplatform-connect-to-apis.html) or dependency injection to provide a
    platform-specific log file path.

## Custom logging

Couchbase Lite allows for the registration of a callback function to receive Couchbase Lite log messages, which may be
logged using any external logging framework.

To do this, apps must implement the [`Logger`](/api/couchbase-lite-ee/kotbase/-logger/) interface — see [Example 3
](#example-3) — and enable custom logging using [`Log.custom`](/api/couchbase-lite-ee/kotbase/-log/custom.html) — see
[Example 4](#example-4).

!!! example "<span id='example-3'>Example 3. Implementing logger interface</span>"

    Here we introduce the code that implements the [`Logger`](/api/couchbase-lite-ee/kotbase/-logger/) interface.

    ```kotlin
    class LogTestLogger(override val level: LogLevel) : Logger {
        override fun log(level: LogLevel, domain: LogDomain, message: String) {
            // this method will never be called if param level < this.level
            // handle the message, for example piping it to a third party framework
        }
    }
    ```

!!! example "<span id='example-4'>Example 4. Enabling custom logging</span>"

    This example show how to enable the custom logger from [Example 3](#example-3).

    ```kotlin
    // this custom logger will not log an event with a log level < WARNING
    Database.log.custom = LogTestLogger(LogLevel.WARNING) 
    ```

Here we set the custom logger with a level of `WARNING`. The custom logger is called with every log and may choose to
filter it, using its configured level.

## Decoding binary logs

You can use the **cbl-log** tool to decode binary log files — see [Example 5](#example-5).

!!! example "<span id='example-5'>Example 5. Using the cbl-log tool</span>"

    === "macOS"

        Download the **cbl-log** tool using `wget`.

        ```title="console"
        wget https://packages.couchbase.com/releases/couchbase-lite-log/3.0.0/couchbase-lite-log-3.0.0-macos.zip
        ```

        Extract the downloaded zip file.

        ```title="console"
        unzip couchbase-lite-log-3.0.0-macos.zip
        ```

        Navigate to the **bin** directory and run the `cbl-log` executable.

        ```title="console"
        ./cbl-log logcat LOGFILE <OUTPUT_PATH>
        ```

    === "CentOS"

        Download the **cbl-log** tool using `wget`.

        ```title="console"
        wget https://packages.couchbase.com/releases/couchbase-lite-log/3.0.0/couchbase-lite-log-3.0.0-centos.zip
        ```

        Extract the downloaded zip file.

        ```title="console"
        unzip couchbase-lite-log-3.0.0-centos.zip
        ```

        Navigate to the **bin** directory and run the `cbl-log` executable.

        ```title="console"
        ./cbl-log logcat LOGFILE <OUTPUT_PATH>
        ```

    === "Windows"

        Download the **cbl-log** tool using PowerShell.

        ```title="PowerShell"
        Invoke-WebRequest https://packages.couchbase.com/releases/couchbase-lite-log/3.0.0/couchbase-lite-log-3.0.0-windows.zip -OutFile couchbase-lite-log-3.0.0-windows.zip
        ```

        Extract the downloaded zip file.

        ```title="PowerShell"
        Expand-Archive -Path couchbase-lite-log-3.0.0-windows.zip -DestinationPath .
        ```

        Run the cbl-log executable.

        ```title="PowerShell"
        .\cbl-log.exe logcat LOGFILE <OUTPUT_PATH>
        ```
