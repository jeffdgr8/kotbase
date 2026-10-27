//
//  CBLMessageEndpoint.h
//  CouchbaseLite
//
//  Copyright (c) 2024 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//


#import <CouchbaseLite/CBLEndpoint.h>
#import <CouchbaseLite/CBLProtocolType.h>

@protocol CBLMessageEndpointConnection;
@protocol CBLMessageEndpointDelegate;

NS_ASSUME_NONNULL_BEGIN

/**
 ENTERPRISE EDITION ONLY.
 
 Message endpoint.
 */
@interface CBLMessageEndpoint : NSObject <CBLEndpoint>

/**
 The unique identifier of the endpoint.
 */
@property (nonatomic, readonly, copy) NSString* uid;

/**
 The target used for storing any arbitary value representing the endpoint.
 */
@property (nonatomic, readonly, nullable) id target;

/**
 The data transportation protocol.
 */
@property (nonatomic, readonly) CBLProtocolType protocolType;

/**
 The delegate for creating CBLMessageEndpointConnection object.
 */
@property (nonatomic, weak, readonly) id<CBLMessageEndpointDelegate> delegate;

/**
 Initializes a CBLMessageEndpoint object.

 @param uid The unique identifier of the endpoint.
 @param target An optional arbitrary object that represents the endpoint.
 @param protocolType The data transportation protocol.
 @param delegate The delegate for creating CBLMessageEndpointConnection objects.
 @return The CBLMessageEndpoint object.
 */
- (instancetype) initWithUID: (NSString*)uid
                      target: (nullable id)target
                protocolType: (CBLProtocolType)protocolType
                    delegate: (id<CBLMessageEndpointDelegate>)delegate;

/** Not available */
- (instancetype) init NS_UNAVAILABLE;

@end

/**
 ENTERPRISE EDITION ONLY.
 
 A delegate used by the replicator to create CBLMessageEndpointConnection objects.
 */
@protocol CBLMessageEndpointDelegate

/**
 Creates an object of type CBLMessageEndpointConnection protocol.
 An application implements the CBLMessageEndpointConnection protocol using a
 custom transportation method such as using the MultipeerConnectivity framework
 to exchange replication data with the endpoint.
 
 @param endpoint The endpoint object.
 @return The CBLMessageEndpointConnection object.
 */
- (id <CBLMessageEndpointConnection>) createConnectionForEndpoint: (CBLMessageEndpoint*)endpoint;

@end

NS_ASSUME_NONNULL_END
