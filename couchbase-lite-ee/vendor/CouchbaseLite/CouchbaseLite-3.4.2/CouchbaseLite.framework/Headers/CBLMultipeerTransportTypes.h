//
//  CBLMultipeerTransportTypes.h
//  CouchbaseLite
//
//  Copyright (c) 2026 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/** A set of transport types (bitmask). Combine values with the bitwise OR operator
    (e.g., WiFi | Bluetooth) to represent multiple transports. */
typedef NS_OPTIONS(NSUInteger, CBLMultipeerTransportSet) {
    /** No specific transport. Used only in `CBLMultipeerReplicatorStatus` to represent
        the aggregated status of all transports. */
    kCBLMultipeerTransportNone          = 0,
    /** Wi-Fi using DNS-SD for peer discovery. */
    kCBLMultipeerTransportWifi          = 1 << 0,
    /** Bluetooth Low Energy (BLE). */
    kCBLMultipeerTransportBluetooth     = 1 << 1
};

/** Transport type for peer discovery and replication (single value).
 
    @note
    In the API, `CBLMultipeerTransport` represents a single transport, while `CBLMultipeerTransportSet`
    represents one or more transports as a bitmask. */
typedef CBLMultipeerTransportSet CBLMultipeerTransport;

NS_ASSUME_NONNULL_END
