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
@file:Suppress(
    "NO_EXPLICIT_VISIBILITY_IN_API_MODE_WARNING",
    "NO_EXPLICIT_RETURN_TYPE_IN_API_MODE_WARNING"
)

package kotbase

object CBLError {

    /**
     * The error type: roughly, where it originated.
     */
    object Domain {

        const val CBLITE = "CouchbaseLite"
        const val POSIX = "POSIXErrorDomain"
        const val SQLITE = "CouchbaseLite.SQLite"
        const val FLEECE = "CouchbaseLite.Fleece"
    }

    // Error Code
    object Code {

        /**
         * Internal assertion failure
         */
        const val ASSERTION_FAILED = 1

        /**
         * An unimplemented API call
         */
        const val UNIMPLEMENTED = 2

        /**
         * Unsupported encryption algorithm
         */
        const val UNSUPPORTED_ENCRYPTION = 3

        /**
         * An attempt was made to insert a document with an invalid revision ID
         * This is frequently caused by an invalid revision ID written directly into Sync Gateway via the REST API
         */
        const val BAD_REVISION_ID = 4

        /**
         * Revision contains corrupted/unreadable data
         */
        const val CORRUPT_REVISION_DATA = 5

        /**
         * Database/KeyStore is not open
         */
        const val NOT_OPEN = 6

        /**
         * Document not found
         */
        const val NOT_FOUND = 7

        /**
         * Document update conflict
         */
        const val CONFLICT = 8

        /**
         * Invalid function parameter or struct value
         */
        const val INVALID_PARAMETER = 9

        /**
         * Internal unexpected C++ exception
         */
        const val UNEXPECTED_ERROR = 10

        /**
         * Database file can't be opened; may not exist
         */
        const val CANT_OPEN_FILE = 11

        /**
         * File I/O error
         */
        const val IO_ERROR = 12

        /**
         * Memory allocation failed (out of memory?)
         */
        const val MEMORY_ERROR = 13

        /**
         * File is not writeable
         */
        const val NOT_WRITABLE = 14

        /**
         * Data is corrupted
         */
        const val CORRUPT_DATA = 15

        /**
         * Database is busy / locked
         */
        const val BUSY = 16

        /**
         * Function cannot be called while in a transaction
         */
        const val NOT_IN_TRANSACTION = 17

        /**
         * Database can't be closed while a transaction is open
         */
        const val TRANSACTION_NOT_CLOSED = 18

        /**
         * Operation not supported on this database
         */
        const val UNSUPPORTED = 19

        /**
         * File is not a database  or encryption key is wrong
         */
        const val NOT_A_DATABASE_FILE = 20

        /**
         * Database exists but not in the format/storage requested
         */
        const val WRONG_FORMAT = 21

        /**
         * Encryption / Decryption error
         */
        const val CRYPTO = 22

        /**
         * Invalid query
         */
        const val INVALID_QUERY = 23

        /**
         * No such index, or query requires a nonexistent index
         */
        const val MISSING_INDEX = 24

        /**
         * Unknown query param name, or param number out of range
         */
        const val INVALID_QUERY_PARAM = 25

        /**
         * Unknown error from remote server
         */
        const val REMOTE_ERROR = 26

        /**
         * Database file format is older than what I can open
         */
        const val DATABASE_TOO_OLD = 27

        /**
         * Database file format is newer than what I can open
         */
        const val DATABASE_TOO_NEW = 28

        /**
         * Invalid document ID
         */
        const val BAD_DOC_ID = 29

        /**
         * Database can't be upgraded (might be unsupported dev version)
         */
        const val CANT_UPGRADE_DATABASE = 30

        // --- Network status codes start here
        // Network error codes (higher level than POSIX, lower level than HTTP.)
        const val NETWORK_OFFSET = 5000

        /**
         * DNS Lookup failed
         */
        const val DNS_FAILURE = NETWORK_OFFSET + 1

        /**
         * DNS server doesn't know the hostname
         */
        const val UNKNOWN_HOST = NETWORK_OFFSET + 2

        /**
         * Socket timeout during an operation
         */
        const val TIMEOUT = NETWORK_OFFSET + 3

        /**
         * The provided URL is not valid
         */
        const val INVALID_URL = NETWORK_OFFSET + 4

        /**
         * Too many HTTP redirects for the HTTP client to handle
         */
        const val TOO_MANY_REDIRECTS = NETWORK_OFFSET + 5

        /**
         * Failure during TLS handshake process
         */
        const val TLS_HANDSHAKE_FAILED = NETWORK_OFFSET + 6

        /**
         * The provided TLS certificate has expired
         */
        const val TLS_CERT_EXPIRED = NETWORK_OFFSET + 7

        /**
         * Cert isn't trusted for other reason
         */
        const val TLS_CERT_UNTRUSTED = NETWORK_OFFSET + 8

        /**
         * A required client certificate was not provided
         */
        const val TLS_CLIENT_CERT_REQUIRED = NETWORK_OFFSET + 9

        /**
         * Client certificate was rejected by the server
         */
        const val TLS_CLIENT_CERT_REJECTED = NETWORK_OFFSET + 10

        /**
         * Self-signed cert, or unknown anchor cert
         */
        const val TLS_CERT_UNKNOWN_ROOT = NETWORK_OFFSET + 11

        /**
         * The client was redirected to an invalid location by the server
         */
        const val INVALID_REDIRECT = NETWORK_OFFSET + 12

        // --- HTTP status codes start here
        const val HTTP_BASE = 10000

        /**
         * Missing or incorrect user authentication
         */
        const val HTTP_AUTH_REQUIRED = HTTP_BASE + 401

        /**
         * User doesn't have permission to access resource
         */
        const val HTTP_FORBIDDEN = HTTP_BASE + 403

        /**
         * Resource not found
         */
        const val HTTP_NOT_FOUND = HTTP_BASE + 404

        /**
         * Update conflict
         */
        const val HTTP_CONFLICT = HTTP_BASE + 409

        /**
         * HTTP proxy requires authentication
         */
        const val HTTP_PROXY_AUTH_REQUIRED = HTTP_BASE + 407

        /**
         * Data is too large to upload
         */
        const val HTTP_ENTITY_TOO_LARGE = HTTP_BASE + 413

        const val HTTP_IM_A_TEAPOT = HTTP_BASE + 418

        /**
         * Something's wrong with the server
         */
        const val HTTP_INTERNAL_SERVER_ERROR = HTTP_BASE + 500

        /**
         * Unimplemented server functionality
         */
        const val HTTP_NOT_IMPLEMENTED = HTTP_BASE + 501

        /**
         * Service is down temporarily
         */
        const val HTTP_SERVICE_UNAVAILABLE = HTTP_BASE + 503

        // --- Web socket status codes start here
        /**
         * Not an actual error, but serves as the lower bound for WebSocket related errors
         */
        const val WEB_SOCKET_NORMAL_CLOSE = HTTP_BASE + 1000

        /**
         * Peer has to close, e.g. because host app is quitting
         */
        const val WEB_SOCKET_GOING_AWAY = HTTP_BASE + 1001

        /**
         * Protocol violation: invalid framing data
         */
        const val WEB_SOCKET_PROTOCOL_ERROR = HTTP_BASE + 1002

        /**
         * Message payload cannot be handled
         */
        const val WEB_SOCKET_DATA_ERROR = HTTP_BASE + 1003

        /**
         * TCP socket closed unexpectedly
         */
        const val WEB_SOCKET_ABNORMAL_CLOSE = HTTP_BASE + 1006

        /**
         * Unparseable WebSocket message
         */
        const val WEB_SOCKET_BAD_MESSAGE_FORMAT = HTTP_BASE + 1007

        /**
         * Message violated unspecified policy
         */
        const val WEB_SOCKET_POLICY_ERROR = HTTP_BASE + 1008

        /**
         * Message is too large for peer to handle
         */
        const val WEB_SOCKET_MESSAGE_TOO_BIG = HTTP_BASE + 1009

        /**
         * Peer doesn't provide a necessary extension
         */
        const val WEB_SOCKET_MISSING_EXTENSION = HTTP_BASE + 1010

        /**
         * Can't fulfill request due to "unexpected condition"
         */
        const val WEB_SOCKET_CANT_FULFILL = HTTP_BASE + 1011

        const val WEB_SOCKET_TLS_FAILURE = HTTP_BASE + 1015

        const val WEB_SOCKET_USER = HTTP_BASE + 4000

        /**
         * Exceptions during P2P replication that are transient will be assigned this error code
         */
        const val WEB_SOCKET_CLOSE_USER_TRANSIENT = HTTP_BASE + 4001

        /**
         * Exceptions during P2P replication that are permanent will be assigned this error code
         */
        const val WEB_SOCKET_CLOSE_USER_PERMANENT = HTTP_BASE + 4002
    }
}
