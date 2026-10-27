//
//  CBLPeerInfo.h
//  CouchbaseLite
//
//  Copyright (c) 2025 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>
#import <CouchbaseLite/CBLMultipeerTransportTypes.h>

@class CBLPeerID;
@class CBLReplicatorStatus;

NS_ASSUME_NONNULL_BEGIN

/** Represents information about a peer in the multipeer mesh network. */
@interface CBLPeerInfo : NSObject

/** The peer identifier. */
@property (nonatomic, readonly) CBLPeerID* peerID;

/**
 The peer’s TLS certificate.
 
 This is only available after the peer has been authenticated during the replication connection process.
 */
@property (nonatomic, readonly, nullable) SecCertificateRef certificate;

/** Indicates whether the peer is currently online (visible) or offline. */
@property (nonatomic, readonly) BOOL online;

/** The transports on which the peer was discovered. */
@property (nonatomic, readonly) CBLMultipeerTransportSet transports;

/** The transport currently used for replication. */
@property (nonatomic, readonly) CBLMultipeerTransport replicatorTransport;

/** The current replication status for this peer.  */
@property (nonatomic, readonly) CBLReplicatorStatus* replicatorStatus;

/** A list of the peer’s current neighboring peers. */
@property (nonatomic, readonly) NSArray<CBLPeerID*>* neighborPeers;

/** Not available. */
- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END

