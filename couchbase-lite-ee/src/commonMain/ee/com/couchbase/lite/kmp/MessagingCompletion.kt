package com.couchbase.lite.kmp

/**
 * **ENTERPRISE EDITION API**
 *
 * The MessagingCompletion callback interface used for acknowledging
 * the completion of a messaging operation.
 *
 * Acknowledges completion of the operation.
 *
 * @param success Success or failure
 * @param error   The error if any
 */
public typealias MessagingCompletion = (success: Boolean, error: MessagingError?) -> Unit
