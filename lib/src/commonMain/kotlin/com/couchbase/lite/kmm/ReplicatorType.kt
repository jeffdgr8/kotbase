package com.couchbase.lite.kmm

/**
 * The replication direction
 *
 * PUSH_AND_PULL: Bidirectional; both push and pull
 * PUSH: Pushing changes to the target
 * PULL: Pulling changes from the target
 */
public expect enum class ReplicatorType {
    PUSH_AND_PULL,
    PUSH,
    PULL
}