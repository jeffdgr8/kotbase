package com.couchbase.lite.kmp

/**
 * **ENTERPRISE EDITION API**
 *
 * The protocol type of the data transportation.
 */
public expect enum class ProtocolType {

    /**
     * MESSAGE protocol means that Core callbacks contain exactly
     * the data that needs to be transferred.  Core does not format
     * the data in any way.
     */
    MESSAGE_STREAM,

    /**
     * BYTE protocol means that Core knows that this is a web socket
     * connection.  The data with which Core calls us contains the
     * properly framed message, heartbeats and so on.
     * ... we don't use this because that would be too easy, right?
     * OkHTTP also wants to frame the data.
     */
    BYTE_STREAM
}
