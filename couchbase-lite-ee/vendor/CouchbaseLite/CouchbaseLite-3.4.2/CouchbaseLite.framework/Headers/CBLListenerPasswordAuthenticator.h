//
//  CBLListenerPasswordAuthenticator.h
//  CouchbaseLite
//
//  Copyright (c) 2024 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>
#import <CouchbaseLite/CBLListenerAuthenticator.h>

NS_ASSUME_NONNULL_BEGIN

/**
 ENTERPRISE EDITION ONLY.
 
 Password Authenticator Block for verifying client credentials.
 */
typedef BOOL (^CBLListenerPasswordAuthenticatorBlock) (NSString* username, NSString* password);

/**
 ENTERPRISE EDITION ONLY.
 
 Password Authenticator for the CBLURLEndpointListener.
 */
@interface CBLListenerPasswordAuthenticator : NSObject<CBLListenerAuthenticator>

/** Initialize with a password authenticator block for verifying client credentials */
- (instancetype) initWithBlock: (CBLListenerPasswordAuthenticatorBlock)authBlock;

/** Not available. */
- (instancetype) init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
