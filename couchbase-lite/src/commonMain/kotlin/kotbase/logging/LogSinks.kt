/*
 * Copyright 2025 Jeff Lockhart
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
package kotbase.logging

public expect object LogSinks {

    /**
     * The File Log Sink: a sink that writes log messages to the
     * Couchbase Lite Mobile File logger.
     */
    public var file: FileLogSink?

    /**
     * The Console Log Sink: a sink that writes log messages to system console.
     */
    public var console: ConsoleLogSink?

    /**
     * The Custom Log Sink: a user-defined log sink that can forward log messages
     * to a custom destination.
     *
     * Note that logging to the Custom Logger is asynchronous.
     * A logger may receive several log messages after it has been removed
     * or replaced as the current logger.
     */
    public var custom: CustomLogSink?
}
