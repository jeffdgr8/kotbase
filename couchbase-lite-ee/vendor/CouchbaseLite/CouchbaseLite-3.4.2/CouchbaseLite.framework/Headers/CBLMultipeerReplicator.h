//
//  CBLMultipeerReplicator.h
//  CouchbaseLite
//
//  Copyright (c) 2025 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>
#import <CouchbaseLite/CBLMultipeerTransportTypes.h>

@class CBLMultipeerReplicatorConfiguration;
@class CBLMultipeerReplicatorStatus;
@class CBLPeerDiscoveryStatus;
@class CBLPeerDocumentReplication;
@class CBLPeerID;
@class CBLPeerInfo;
@class CBLPeerReplicatorStatus;

@protocol CBLListenerToken;

NS_ASSUME_NONNULL_BEGIN

/**
 A multipeer replicator that manages peer discovery, connection, and replication in
 a multipeer mesh network.

 The replicator supports the Wi-Fi and Bluetooth LE transports, which can be enabled
 using the configuration's `transports` property. By default, only Wi-Fi is enabled.
 Each enabled transport has its own app requirements:
 - Wi-Fi : Add `NSLocalNetworkUsageDescription` and `NSBonjourServices` with
   `_couchbaseP2P._tcp` to the Info.plist.
 - Bluetooth : Add `NSBluetoothAlwaysUsageDescription` to the Info.plist, and link
   CoreBluetooth.framework.
 */
@interface CBLMultipeerReplicator : NSObject

/** The configuration. */
@property (nonatomic, readonly, copy) CBLMultipeerReplicatorConfiguration* config;

/** The peer identifier represents this multipeer replicator instance. */
@property (nonatomic, readonly) CBLPeerID* peerID;

/**
 A list of currently visible peer identifiers.

 @warning This property can block for several seconds in some cases. It is recommended to not
 access it on the UI thread. This is a known issue we plan to address in a future release.
 */
@property (nonatomic, readonly) NSArray<CBLPeerID*>* neighborPeers;

/**
 Initializes the multipeer replicator with the given configuration.
 
 @param config The configuration.
 @param error On return, the error if any.
 @return An initialized replicator instance, or nil on failure.
 */
- (nullable instancetype) initWithConfig: (CBLMultipeerReplicatorConfiguration*)config
                                   error: (NSError**)error;

/**
 Starts peer discovery and replication with connected peers.
 @note Once stopped, restarting is not currently supported.
 */
- (void) start;

/**
 Stops peer discovery and all active replicators.
 @note Once stopped, restarting is not currently supported.
 */
- (void) stop;

/**
 Returns information about a peer.

 @warning This method can block for several seconds in some cases. It is recommended to not
 call this on the UI thread. This is a known issue we plan to address in a future release.

 @param peerID The peer identifier.
 @return The peer info instance, or nil if the peer is unknown.
 */
- (nullable CBLPeerInfo*) peerInfoForPeerID: (CBLPeerID*)peerID;

/**
 Returns the current status of the given transport, or nil if that transport is not enabled.

 @warning This method can block for several seconds in some cases. It is recommended to not
 call this on the UI thread. This is a known issue we plan to address in a future release.

 @param transport The transport to query for the status.
 @return The current status, or nil if the transport is not enabled.
 */
- (nullable CBLMultipeerReplicatorStatus*) statusForTransport: (CBLMultipeerTransport)transport;

/**
 Adds a listener for changes to the overall multipeer replicator status.

 @param queue The dispatch queue for invoking the listener. If nil, the main queue is used.
 @param listener A block to receive status updates.
 @return A token for removing the listener.
 */
- (id<CBLListenerToken>) addStatusListenerWithQueue: (nullable dispatch_queue_t)queue
                                           listener: (void (^)(CBLMultipeerReplicatorStatus*))listener;

/**
 Adds a listener for updates to peer discovery status.

 @param queue The dispatch queue for invoking the listener. If nil, the main queue is used.
 @param listener A block to receive discovery status updates.
 @return A token for removing the listener.
 */
- (id<CBLListenerToken>) addPeerDiscoveryStatusListenerWithQueue: (nullable dispatch_queue_t)queue
                                                        listener: (void (^)(CBLPeerDiscoveryStatus*))listener;

/**
 Adds a listener for replicator status updates for each connected peer.

 @param queue The dispatch queue for invoking the listener. If nil, the main queue is used.
 @param listener A block to receive per-peer replicator status updates.
 @return A token for removing the listener.
 */
- (id<CBLListenerToken>) addPeerReplicatorStatusListenerWithQueue: (nullable dispatch_queue_t)queue
                                                         listener: (void (^)(CBLPeerReplicatorStatus*))listener;

/**
 Adds a listener for document replication updates from each connected peer.

 @param queue The dispatch queue for invoking the listener. If nil, the main queue is used.
 @param listener A block to receive per-peer document replication updates.
 @return A token for removing the listener.
 */
- (id<CBLListenerToken>) addPeerDocumentReplicationListenerWithQueue: (nullable dispatch_queue_t)queue
                                                            listener: (void (^)(CBLPeerDocumentReplication*))listener;

/** Not available. */
- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
