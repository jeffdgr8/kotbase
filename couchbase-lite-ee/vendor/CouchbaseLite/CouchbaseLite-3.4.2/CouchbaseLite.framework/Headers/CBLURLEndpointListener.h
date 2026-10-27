//
//  CBLURLEndpointListener.h
//  CouchbaseLite
//
//  Copyright (c) 2024 Couchbase, Inc All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>

@class CBLURLEndpointListenerConfiguration;
@class CBLTLSIdentity;

NS_ASSUME_NONNULL_BEGIN

/**
 ENTERPRISE EDITION ONLY.
 
 CBLConnectionStatus provides the information about the connections from the
 replicators to the listener.
 */
typedef struct {
    uint64_t connectionCount;       ///< The total number of connections.
    uint64_t activeConnectionCount; ///< The number of the connections that are in active or busy state.
} CBLConnectionStatus;

/**
 ENTERPRISE EDITION ONLY.
 
A listener to provide websocket based endpoint for peer-to-peer replication.
Once the listener is started, peer replicators can connect to the listener by using
CBLURLEndpoint.
*/
@interface CBLURLEndpointListener : NSObject

/** The read-only configuration. */
@property (nonatomic, readonly) CBLURLEndpointListenerConfiguration* config;

/** The port that the listener is listening to. If the listener is not started, the port property will be zero. */
@property (nonatomic, readonly) unsigned short port;

/** The TLS identity used by the listener to provide TLS communication. If the listener is not started or
    the TLS is disabled, the property value will be nil.  */
@property (nonatomic, readonly, nullable) CBLTLSIdentity* tlsIdentity;

/** The possible URLs of the listener. If the listener is not started, the urls property will be nil. */
@property (nonatomic, readonly, nullable) NSArray<NSURL*>* urls;

/** The current connection status of the listener.  */
@property (nonatomic, readonly) CBLConnectionStatus status;

/** Initializes a listener with the given configuration object. */
- (instancetype) initWithConfig: (CBLURLEndpointListenerConfiguration*)config;

/** Starts the listener. */
- (BOOL) startWithError: (NSError**)error;

/** Stop the listener. */
- (void) stop;

/** Not available. */
- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
