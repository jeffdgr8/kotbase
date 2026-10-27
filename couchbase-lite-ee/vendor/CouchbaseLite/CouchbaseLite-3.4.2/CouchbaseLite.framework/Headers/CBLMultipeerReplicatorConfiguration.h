//
//  CBLMultipeerReplicatorConfiguration.h
//  CouchbaseLite
//
//  Copyright (c) 2025 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>
#import <CouchbaseLite/CBLMultipeerTransportTypes.h>

@class CBLMultipeerCertificateAuthenticator;
@class CBLMultipeerCollectionConfiguration;
@class CBLTLSIdentity;
@protocol CBLMultipeerAuthenticator;

NS_ASSUME_NONNULL_BEGIN

/** Configuration for creating a MultipeerReplicator */
@interface CBLMultipeerReplicatorConfiguration : NSObject

/** Identifier for discovering and connecting peers. */
@property (nonatomic, readonly, copy) NSString* peerGroupID;

/** Peer identity. The identity’s certificate must be both server and client certificate. */
@property (nonatomic, readonly) CBLTLSIdentity* identity;

/** Peer authenticator for verifying the connecting peer's certificate. */
@property (nonatomic, readonly) id<CBLMultipeerAuthenticator> authenticator;

/** A list of collection configurations for collections to replicate. */
@property (nonatomic, readonly, copy) NSArray<CBLMultipeerCollectionConfiguration*>* collections;

/** Transport options for peer discovery and replication. The default is Wi-Fi only.
    When multiple transports are specified, the replicator automatically selects and switches
    between available transports, preferring Wi-Fi over Bluetooth. */
@property (nonatomic) CBLMultipeerTransportSet transports;

/**
 Initializes the configuration with a peer group identifier, identity, authenticator and collections.

 @param peerGroupID The group identifier for discovering and connecting peers.
 @param identity The peer's TLS identity. The certificate must be valid for both client and server authentication.
 @param authenticator Peer authenticator for verifying the connecting peer's certificate.
 @param collections A list of collection configurations for collections to replicate.
 @return A CBLMultipeerReplicatorConfiguration instance.
 */
- (instancetype) initWithPeerGroupID: (NSString*)peerGroupID
                            identity: (CBLTLSIdentity*)identity
                       authenticator: (id<CBLMultipeerAuthenticator>)authenticator
                         collections: (NSArray<CBLMultipeerCollectionConfiguration*>*)collections;

/** Not available */
- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
