//
//  CBLMultipeerEventTypes.h
//  CouchbaseLite
//
//  Copyright (c) 2025 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>
#import <CouchbaseLite/CBLMultipeerTransportTypes.h>

@class CBLPeerID;
@class CBLReplicationPeer;
@class CBLReplicatorStatus;
@class CBLReplicatedDocument;

NS_ASSUME_NONNULL_BEGIN

/** Multipeer Replicator Status. */
@interface CBLMultipeerReplicatorStatus : NSObject

/**
 The transport this status applies to.
 
 If the value is kCBLMultipeerTransportNone, the status represents the
 aggregate state of the replicator rather than the state of a single transport.
 */
@property (nonatomic, readonly) CBLMultipeerTransport transport;

/**
 Indicates whether the multipeer replicator is currently active.
 This means the replicator is discovering peers or performing replication.
 */
@property (nonatomic, readonly) BOOL active;

/** The error encountered, if any. */
@property (nonatomic, readonly, nullable) NSError* error;

/** Not available. */
- (instancetype) init NS_UNAVAILABLE;

@end

/** Discovery status of a specific peer. */
@interface CBLPeerDiscoveryStatus : NSObject

/** The peer’s identifier. */
@property (nonatomic, readonly) CBLPeerID* peerID;

/** Indicates whether the peer is currently visible (online) or not (offline). */
@property (nonatomic, readonly) BOOL online;

/** The transport on which the peer was discovered. */
@property (nonatomic, readonly) CBLMultipeerTransport transport;

/** Not available. */
- (instancetype) init NS_UNAVAILABLE;

@end

/** The replicator status for a specific peer. */
@interface CBLPeerReplicatorStatus : NSObject

/** The peer’s identifier. */
@property (nonatomic, readonly) CBLPeerID* peerID;

/** The transport currently used for replication */
@property (nonatomic, readonly) CBLMultipeerTransport transport;

/** A flag indicating the direction of the replication. */
@property (nonatomic, readonly) BOOL outgoing;

/** The replicator status and progress. */
@property (nonatomic, readonly) CBLReplicatorStatus* status;

/** Not available. */
- (instancetype) init NS_UNAVAILABLE;

@end

/** Document replication status for a specific peer. */
@interface CBLPeerDocumentReplication : NSObject

/** The peer’s identifier. */
@property (nonatomic, readonly) CBLPeerID* peerID;

/** The transport currently used for replication */
@property (nonatomic, readonly) CBLMultipeerTransport transport;

/** A flag indicating the replication is push or pull. */
@property (nonatomic, readonly) BOOL isPush;

/** The replicated documents. */
@property (nonatomic, readonly) NSArray<CBLReplicatedDocument*>* documents;

/** Not available */
- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
