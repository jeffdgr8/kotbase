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
 * **ENTERPRISE EDITION API**
 *
 * The messaging error.
 *
 * @constructor Creates a MessagingError with the given error and recoverable flag identifying
 * if the error is recoverable or not. The replicator uses recoverable
 * flag to determine whether the replication should be retried or stopped as the error
 * is non-recoverable.
 *
 * @param error       the error
 * @param recoverable the recoverable flag
 */
public expect class MessagingError(error: Exception, recoverable: Boolean) {

    /**
     * Is the error recoverable?
     *
     * The recoverable flag identifying whether the error is recoverable or not
     */
    public val isRecoverable: Boolean

    /**
     * The error object.
     */
    public val error: Exception
}
