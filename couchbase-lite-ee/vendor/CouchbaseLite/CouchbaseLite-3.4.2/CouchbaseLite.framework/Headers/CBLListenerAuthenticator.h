//
//  CBLListenerAuthenticator.h
//  CouchbaseLite
//
//  Copyright (c) 2020 Couchbase, Inc. All rights reserved.
//  COUCHBASE CONFIDENTIAL -- part of Couchbase Lite Enterprise Edition
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 ENTERPRISE EDITION ONLY.
 
 The authenticator used by CBLURLEndpointListener to authenticate clients.
*/
@protocol CBLListenerAuthenticator <NSObject>
@end

NS_ASSUME_NONNULL_END
