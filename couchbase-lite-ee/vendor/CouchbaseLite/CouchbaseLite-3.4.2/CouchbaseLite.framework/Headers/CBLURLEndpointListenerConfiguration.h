//
//  CBLURLEndpointListenerConfiguration.h
//  CouchbaseLite
//
//  Copyright (c) 2024 Couchbase, Inc All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>
#import <CouchbaseLite/CBLListenerAuthenticator.h>

@class CBLDatabase;
@class CBLTLSIdentity;

NS_ASSUME_NONNULL_BEGIN

/**
 ENTERPRISE EDITION ONLY.
 
 The configuration used for configuring and creating a URLEndpointListener.
 */
@interface CBLURLEndpointListenerConfiguration: NSObject

/** The database object. */
@property (nonatomic, readonly) CBLDatabase* database __deprecated_msg("Use collections instead.");

/**
 The port that the listener will listen to. Default value is ``kCBLDefaultListenerPort``
 If the value is zero, it means that the listener will automatically select
 an available port to listen to when the listener is started.
 */
@property (nonatomic) unsigned short port;

/**
 The network interface in the form of the IP Address or network interface name such as en0 that the listener will
 listen to. The default value is nil which means that the listener will listen to all network interfaces.
 */
@property (nonatomic, nullable) NSString* networkInterface;

/**
 Disable TLS communication. The default value is ``kCBLDefaultListenerDisableTls``
 */
@property (nonatomic) BOOL disableTLS;

/**
 The TLS Identity used for configuring TLS Communication. The default value is nil which means that
 a generated anonymous self-signed identity will be used unless the disableTLS property is set to YES.
 */
@property (nonatomic, nullable) CBLTLSIdentity* tlsIdentity;

/**
 The authenticator used by the listener to authenticate clients.
 */
@property (nonatomic, nullable) id<CBLListenerAuthenticator> authenticator;

/**
 Allow delta sync when replicating with the listener.
 The default value is ``kCBLDefaultListenerEnableDeltaSync``.
 */
@property (nonatomic) BOOL enableDeltaSync;

/**
 Allow only pull replication to pull changes from the listener.
 The default value is ``kCBLDefaultListenerReadOnly``.
 */
@property (nonatomic) BOOL readOnly;

/**
 The collections available for replication.
 When the [... initWithDatabase: target:] is used, the collections property will contain
 the default collection.
 */
@property (nonatomic, readonly) NSArray* collections;

/** Init with the database object to make the default collection available for the replication. */
- (id) initWithDatabase: (CBLDatabase*)database
__deprecated_msg("Use [... initWithCollections:] instead.");

/**
 Init with the collections that will be available for replication.
 
 The specified collections must contain at least one collection,
 otherwise an Invalid Argument Exception will be thrown.
 
 Currently only collections from a single scope are supported,
 otherwise an Invalid Argument Exception will be thrown.

 All of the collections must belong to the same database,
 otherwise an Invalid Argument Exception will be thrown.

 If one of the collections is removed during the replication, the listener would be stopped,
 and the connections to the connected clients would be closed with an error - Need to sync
 the behavior and error code with SG.
 
 @param collections list of collection available to sync
 */
- (instancetype) initWithCollections: (NSArray*)collections;

/** Initializes a listener with the configuration object. */
- (instancetype) initWithConfig: (CBLURLEndpointListenerConfiguration*)config;

/** Not available. */
- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
